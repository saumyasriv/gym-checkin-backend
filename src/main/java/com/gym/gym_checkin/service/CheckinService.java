
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

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

        // ============================================================
        // LOCK MEMBER
        // ============================================================
        //
        // Prevents simultaneous requests for the same member from
        // bypassing the cooldown check.
        // ============================================================

        Member member = memberRepository.findByIdForUpdate(
                request.getMemberId()
        ).orElseThrow(() -> {
            log.info(
                    "Member not found with ID: {}",
                    request.getMemberId()
            );

            return new RuntimeException("Member not found");
        });

        // ============================================================
        // CHECK-IN COOLDOWN
        // ============================================================
        //
        // Only successful CHECKIN records trigger the 15-minute
        // cooldown. NO_SHOW does not trigger the cooldown.
        // ============================================================

        if ("CHECKIN".equalsIgnoreCase(type)) {

            LocalDateTime now = LocalDateTime.now(
                    ZoneId.of("Asia/Kolkata")
            );

            LocalDateTime cooldownStart = now.minusMinutes(1);

            Optional<Checkin> latestCheckin =
                    checkinRepository
                            .findTopByMember_IdAndTypeOrderByCheckinTimeDescIdDesc(
                                    request.getMemberId(),
                                    "CHECKIN"
                            );

            if (latestCheckin.isPresent()
                    && latestCheckin.get().getCheckinTime() != null
                    && latestCheckin.get().getCheckinTime()
                    .isAfter(cooldownStart)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This member has already checked in. Please try again after 15 minutes."
                );
            }
        }

        // ============================================================
        // MEMBER TYPE VALIDATION
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

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Kolkata")
        );

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


    public Map<Long, LocalDateTime> getActiveCooldowns() {

        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Kolkata")
        );

        LocalDateTime cooldownStart = now.minusMinutes(15);

        List<Checkin> recentCheckins =
                checkinRepository
                        .findByTypeAndCheckinTimeGreaterThanEqualAndCheckinTimeLessThanOrderByCheckinTimeDesc(
                                "CHECKIN",
                                cooldownStart,
                                now
                        );

        Map<Long, LocalDateTime> activeCooldowns = new HashMap<>();

        for (Checkin checkin : recentCheckins) {

            if (checkin.getMember() != null
                    && checkin.getCheckinTime() != null) {

                Long memberId = checkin.getMember().getId();

                activeCooldowns.putIfAbsent(
                        memberId,
                        checkin.getCheckinTime()
                );
            }
        }

        return activeCooldowns;
    }
}
