package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.CreateMembershipRequest;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.entity.MembershipPauseHistory;
import com.gym.gym_checkin.repository.MemberRepository;
import com.gym.gym_checkin.repository.MembershipPauseHistoryRepository;
import com.gym.gym_checkin.repository.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final MembershipPauseHistoryRepository pauseHistoryRepository;

    public MembershipService(
            MembershipRepository membershipRepository,
            MemberRepository memberRepository,
            MembershipPauseHistoryRepository pauseHistoryRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
        this.pauseHistoryRepository = pauseHistoryRepository;
    }

    public Membership createMembership(CreateMembershipRequest request) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() ->
                        new RuntimeException("Member not found")
                );

        Membership membership = new Membership();

        membership.setMember(member);
        membership.setPackageName(request.getPackageName());
        membership.setAmountPaid(request.getAmountPaid());

        // New memberships are never paused.
        membership.setPaused(false);
        membership.setPauseStartDate(null);

        if (request.getTotalCredits() != null) {

            membership.setTotalCredits(
                    request.getTotalCredits()
            );

            membership.setRemainingCredits(
                    request.getRemainingCredits()
            );

            membership.setStartDate(
                    request.getStartDate()
            );

            membership.setExpiryDate(
                    request.getExpiryDate()
            );

            return membershipRepository.save(membership);
        }

        if ("8 Sessions".equals(request.getPackageName())) {

            membership.setTotalCredits(8);
            membership.setRemainingCredits(8);

            LocalDate startDate = request.getStartDate();

            membership.setStartDate(startDate);
            membership.setExpiryDate(
                    startDate.plusWeeks(5)
            );

        } else if ("12 Sessions".equals(request.getPackageName())) {

            membership.setTotalCredits(12);
            membership.setRemainingCredits(12);

            LocalDate startDate = request.getStartDate();

            membership.setStartDate(startDate);
            membership.setExpiryDate(
                    startDate.plusWeeks(5)
            );

        } else if ("24 Sessions".equals(request.getPackageName())) {

            membership.setTotalCredits(24);
            membership.setRemainingCredits(24);

            LocalDate startDate = request.getStartDate();

            membership.setStartDate(startDate);
            membership.setExpiryDate(
                    startDate.plusWeeks(10)
            );

        } else if ("36 Sessions".equals(request.getPackageName())) {

            membership.setTotalCredits(36);
            membership.setRemainingCredits(36);

            LocalDate startDate = request.getStartDate();

            membership.setStartDate(startDate);
            membership.setExpiryDate(
                    startDate.plusWeeks(15)
            );
        }

        return membershipRepository.save(membership);
    }

    @Transactional
    public Membership pauseMembership(Long membershipId) {

        Membership membership =
                membershipRepository.findById(membershipId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Membership not found"
                                )
                        );

        if (membership.isPaused()) {
            throw new RuntimeException(
                    "Membership is already paused"
            );
        }

        /*
         * Store the current pause state on the membership.
         *
         * This is used to determine that the membership is
         * currently paused.
         */
        LocalDate pauseStartDate = LocalDate.now();

        membership.setPaused(true);
        membership.setPauseStartDate(pauseStartDate);

        /*
         * Create a new open pause-history record.
         *
         * pauseEndDate, pausedDays and daysAddedToExpiry
         * remain null until the membership is unpaused.
         */
        MembershipPauseHistory pauseHistory =
                new MembershipPauseHistory();

        pauseHistory.setMembership(membership);
        pauseHistory.setPauseStartDate(pauseStartDate);
        pauseHistory.setPauseEndDate(null);
        pauseHistory.setPausedDays(null);
        pauseHistory.setDaysAddedToExpiry(null);

        pauseHistoryRepository.save(pauseHistory);

        return membershipRepository.save(membership);
    }

    @Transactional
    public Membership unpauseMembership(Long membershipId) {

        Membership membership =
                membershipRepository.findById(membershipId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Membership not found"
                                )
                        );

        if (!membership.isPaused()) {
            throw new RuntimeException(
                    "Membership is not paused"
            );
        }

        LocalDate pauseStartDate =
                membership.getPauseStartDate();

        if (pauseStartDate == null) {
            throw new RuntimeException(
                    "Pause start date is missing"
            );
        }

        LocalDate today = LocalDate.now();

        /*
         * Calculate the exact number of calendar days
         * between the pause start and unpause date.
         *
         * Example:
         *
         * Pause:   15 Sep
         * Unpause: 22 Sep
         *
         * Result:  7 days
         */
        long pausedDays = ChronoUnit.DAYS.between(
                pauseStartDate,
                today
        );

        /*
         * Find the currently open pause-history record.
         *
         * We look for the most recent history entry whose
         * pauseEndDate is still null.
         */
        MembershipPauseHistory pauseHistory =
                membership.getPauseHistory()
                        .stream()
                        .filter(history ->
                                history.getPauseEndDate() == null
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Pause history record not found"
                                )
                        );

        /*
         * Add the paused duration to the existing expiry date.
         *
         * Credits remain completely untouched.
         */
        if (membership.getExpiryDate() != null) {

            membership.setExpiryDate(
                    membership.getExpiryDate()
                            .plusDays(pausedDays)
            );
        }

        /*
         * Complete the historical pause record.
         */
        pauseHistory.setPauseEndDate(today);
        pauseHistory.setPausedDays(
                Math.toIntExact(pausedDays)
        );
        pauseHistory.setDaysAddedToExpiry(
                Math.toIntExact(pausedDays)
        );

        pauseHistoryRepository.save(pauseHistory);

        /*
         * Clear the current pause state.
         *
         * The completed pause remains safely stored in
         * membership_pause_history.
         */
        membership.setPaused(false);
        membership.setPauseStartDate(null);

        return membershipRepository.save(membership);
    }
}