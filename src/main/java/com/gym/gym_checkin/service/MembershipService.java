package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.CreateMembershipRequest;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.entity.MembershipExpiryHistory;
import com.gym.gym_checkin.entity.MembershipPauseHistory;
import com.gym.gym_checkin.repository.MemberRepository;
import com.gym.gym_checkin.repository.MembershipExpiryHistoryRepository;
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
    private final MembershipExpiryHistoryRepository expiryHistoryRepository;

    public MembershipService(
            MembershipRepository membershipRepository,
            MemberRepository memberRepository,
            MembershipPauseHistoryRepository pauseHistoryRepository,
            MembershipExpiryHistoryRepository expiryHistoryRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
        this.pauseHistoryRepository = pauseHistoryRepository;
        this.expiryHistoryRepository = expiryHistoryRepository;
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

        // New memberships always have expiry enabled.
        membership.setExpiryRemoved(false);
        membership.setExpiryRemovedDate(null);

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
         * A membership with expiry removed cannot be paused.
         *
         * Expiry removal and pause are mutually exclusive.
         */
        if (membership.isExpiryRemoved()) {
            throw new RuntimeException(
                    "Cannot pause a membership while expiry is removed"
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

    @Transactional
    public Membership removeExpiry(Long membershipId) {

        Membership membership =
                membershipRepository.findById(membershipId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Membership not found"
                                )
                        );

        /*
         * Expiry removal is only available for active
         * memberships.
         */
        if (membership.isPaused()) {
            throw new RuntimeException(
                    "Cannot remove expiry from a paused membership"
            );
        }

        if (membership.isExpiryRemoved()) {
            throw new RuntimeException(
                    "Expiry is already removed"
            );
        }

        if (membership.getRemainingCredits() == null
                || membership.getRemainingCredits() <= 0) {
            throw new RuntimeException(
                    "Membership has no credits"
            );
        }

        if (membership.getExpiryDate() == null) {
            throw new RuntimeException(
                    "Membership has no expiry date"
            );
        }

        LocalDate today = LocalDate.now();

        if (membership.getExpiryDate().isBefore(today)) {
            throw new RuntimeException(
                    "Membership has expired"
            );
        }

        /*
         * Store when expiry was removed.
         *
         * The original expiryDate is deliberately NOT changed.
         */
        membership.setExpiryRemoved(true);
        membership.setExpiryRemovedDate(today);

        /*
         * Create an open expiry-history record.
         *
         * The restored date and duration values will remain
         * null until expiry is restored.
         */
        MembershipExpiryHistory expiryHistory =
                new MembershipExpiryHistory();

        expiryHistory.setMembership(membership);
        expiryHistory.setExpiryRemovedDate(today);
        expiryHistory.setExpiryRestoredDate(null);
        expiryHistory.setDaysWithoutExpiry(null);
        expiryHistory.setDaysAddedToExpiry(null);

        expiryHistoryRepository.save(expiryHistory);

        return membershipRepository.save(membership);
    }

    @Transactional
    public Membership restoreExpiry(Long membershipId) {

        Membership membership =
                membershipRepository.findById(membershipId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Membership not found"
                                )
                        );

        if (!membership.isExpiryRemoved()) {
            throw new RuntimeException(
                    "Expiry is already enabled"
            );
        }

        LocalDate expiryRemovedDate =
                membership.getExpiryRemovedDate();

        if (expiryRemovedDate == null) {
            throw new RuntimeException(
                    "Expiry removed date is missing"
            );
        }

        if (membership.getExpiryDate() == null) {
            throw new RuntimeException(
                    "Original expiry date is missing"
            );
        }

        LocalDate today = LocalDate.now();

        /*
         * Calculate how many calendar days expiry was removed.
         *
         * Example:
         *
         * Removed:  10 Sep
         * Restored: 18 Sep
         *
         * Result:   8 days
         */
        long daysWithoutExpiry = ChronoUnit.DAYS.between(
                expiryRemovedDate,
                today
        );

        /*
         * Add the exact number of days to the original
         * expiry date.
         */
        membership.setExpiryDate(
                membership.getExpiryDate()
                        .plusDays(daysWithoutExpiry)
        );

        /*
         * Find the currently open expiry-history record.
         */
        MembershipExpiryHistory expiryHistory =
                membership.getExpiryHistory()
                        .stream()
                        .filter(history ->
                                history.getExpiryRestoredDate() == null
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expiry history record not found"
                                )
                        );

        /*
         * Complete the expiry-history record.
         */
        expiryHistory.setExpiryRestoredDate(today);
        expiryHistory.setDaysWithoutExpiry(
                Math.toIntExact(daysWithoutExpiry)
        );
        expiryHistory.setDaysAddedToExpiry(
                Math.toIntExact(daysWithoutExpiry)
        );

        expiryHistoryRepository.save(expiryHistory);

        /*
         * Restore normal expiry behavior.
         */
        membership.setExpiryRemoved(false);
        membership.setExpiryRemovedDate(null);

        return membershipRepository.save(membership);
    }
}