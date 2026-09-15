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

        List<Membership> memberships =
                membershipRepository
                        .findByMemberAndRemainingCreditsGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                                member,
                                0,
                                LocalDate.now()
                        );

        // Never allow a paused membership to be used.
        memberships = memberships.stream()
                .filter(membership -> !membership.isPaused())
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
                                    membership.getRemainingCredits() != null
                                            && membership.getRemainingCredits() > 0
                                            && membership.getExpiryDate() != null
                                            && membership.getExpiryDate()
                                            .isBefore(LocalDate.now())
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

        Membership membership = memberships.get(0);

        membership.setRemainingCredits(
                membership.getRemainingCredits() - 1
        );

        membershipRepository.save(membership);

        Checkin checkin = new Checkin();
        checkin.setMember(member);
        checkin.setMembership(membership);
        checkin.setType(type);
        checkin.setClassTiming(request.getClassTiming());

        checkinRepository.save(checkin);

        if (membership.getRemainingCredits() < 5) {
            return "Checked in successfully. Warning: only "
                    + membership.getRemainingCredits()
                    + " credits remaining.";
        }

        return "Checked in successfully. Remaining credits: "
                + membership.getRemainingCredits();
    }
}