package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.CheckinRequest;
import com.gym.gym_checkin.entity.Checkin;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.repository.CheckinRepository;
import com.gym.gym_checkin.repository.MemberRepository;
import com.gym.gym_checkin.repository.MembershipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class CheckinService {

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final CheckinRepository checkinRepository;

    public CheckinService(
            MemberRepository memberRepository,
            MembershipRepository membershipRepository,
            CheckinRepository checkinRepository
    ) {
        this.memberRepository = memberRepository;
        this.membershipRepository = membershipRepository;
        this.checkinRepository = checkinRepository;
    }

    @Transactional
    public String processAttendance(
            CheckinRequest request,
            String type
    ) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> {
                    log.info(
                            "Member not found with ID: {}",
                            request.getMemberId()
                    );

                    return new RuntimeException(
                            "Member not found"
                    );
                });

        // ============================================================
        // MEMBER TYPE VALIDATION
        // ============================================================
        //
        // GROUP members can only check in through the GROUP page.
        // PT members can only check in through the PT page.
        //
        // This is enforced on the backend as well as the frontend.
        // ============================================================

        if (request.getMemberType() == null ||
                request.getMemberType().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member type is required"
            );
        }

        if (member.getMemberType() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member classification is missing"
            );
        }

        if (!member.getMemberType()
                .name()
                .equalsIgnoreCase(request.getMemberType())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member type does not match"
            );
        }

        // ============================================================
        // FIND USABLE MEMBERSHIP
        // ============================================================
        //
        // A membership can be used for check-in only when:
        //
        // 1. It has more than 0 credits
        // 2. It is not paused
        // 3. Its expiry date has not passed
        //    OR expiry has been removed
        //
        // If multiple usable memberships exist, use the one
        // expiring earliest.
        // ============================================================

        LocalDate today = LocalDate.now();

        List<Membership> memberships =
                member.getMemberships()
                        .stream()
                        .filter(membership ->
                                membership.getRemainingCredits() != null
                                        && membership.getRemainingCredits() > 0
                        )
                        .filter(membership ->
                                !membership.isPaused()
                        )
                        .filter(membership ->
                                membership.isExpiryRemoved()
                                        || (
                                        membership.getExpiryDate() != null
                                                && !membership.getExpiryDate()
                                                .isBefore(today)
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        membership -> {

                                            /*
                                             * Memberships with no expiry
                                             * are treated as indefinite.
                                             *
                                             * Put them after memberships
                                             * with an actual expiry date so
                                             * expiring credits are consumed
                                             * first.
                                             */
                                            if (membership.isExpiryRemoved()
                                                    || membership.getExpiryDate() == null) {
                                                return LocalDate.MAX;
                                            }

                                            return membership.getExpiryDate();
                                        }
                                )
                        )
                        .toList();

        if (memberships.isEmpty()) {

            boolean hasPausedCredits =
                    member.getMemberships()
                            .stream()
                            .anyMatch(membership ->
                                    membership.isPaused()
                                            && membership.getRemainingCredits() != null
                                            && membership.getRemainingCredits() > 0
                            );

            if (hasPausedCredits) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Membership is paused"
                );
            }

            boolean hasExpiredCredits =
                    member.getMemberships()
                            .stream()
                            .anyMatch(membership ->
                                    !membership.isPaused()
                                            && !membership.isExpiryRemoved()
                                            && membership.getRemainingCredits() != null
                                            && membership.getRemainingCredits() > 0
                                            && membership.getExpiryDate() != null
                                            && membership.getExpiryDate()
                                            .isBefore(today)
                            );

            if (hasExpiredCredits) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Credits expired"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No credits"
            );
        }

        // ============================================================
        // CONSUME CREDIT
        // ============================================================

        Membership membership = memberships.get(0);

        membership.setRemainingCredits(
                membership.getRemainingCredits() - 1
        );

        membershipRepository.save(membership);

        // ============================================================
        // CREATE CHECK-IN
        // ============================================================

        Checkin checkin = new Checkin();

        checkin.setMember(member);

        checkin.setMembership(membership);

        checkin.setType(type);

        checkin.setClassTiming(
                request.getClassTiming()
        );

        checkinRepository.save(checkin);

        // ============================================================
        // RESPONSE
        // ============================================================

        if (membership.getRemainingCredits() < 5) {

            return "Checked in successfully. Warning: only "
                    + membership.getRemainingCredits()
                    + " credits remaining.";
        }

        return "Checked in successfully. Remaining credits: "
                + membership.getRemainingCredits();
    }
}