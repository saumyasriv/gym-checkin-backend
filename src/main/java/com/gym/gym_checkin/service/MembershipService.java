package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.CreateMembershipRequest;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.repository.MemberRepository;
import com.gym.gym_checkin.repository.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;

    public MembershipService(
            MembershipRepository membershipRepository,
            MemberRepository memberRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
    }

    public Membership createMembership(CreateMembershipRequest request) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Membership membership = new Membership();

        membership.setMember(member);
        membership.setPackageName(request.getPackageName());
        membership.setAmountPaid(request.getAmountPaid());

        // New memberships are not paused
        membership.setPaused(false);
        membership.setPauseStartDate(null);

        // If values were supplied
        // (Update Existing Member page)
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

        // Normal Add Membership flow

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

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() ->
                        new RuntimeException("Membership not found")
                );

        if (membership.isPaused()) {
            throw new RuntimeException("Membership is already paused");
        }

        membership.setPaused(true);
        membership.setPauseStartDate(LocalDate.now());

        return membershipRepository.save(membership);
    }

    @Transactional
    public Membership unpauseMembership(Long membershipId) {

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() ->
                        new RuntimeException("Membership not found")
                );

        if (!membership.isPaused()) {
            throw new RuntimeException("Membership is not paused");
        }

        LocalDate pauseStartDate = membership.getPauseStartDate();

        if (pauseStartDate == null) {
            throw new RuntimeException(
                    "Pause start date is missing"
            );
        }

        LocalDate today = LocalDate.now();

        long pausedDays =
                java.time.temporal.ChronoUnit.DAYS.between(
                        pauseStartDate,
                        today
                );

        membership.setExpiryDate(
                membership.getExpiryDate().plusDays(pausedDays)
        );

        membership.setPaused(false);
        membership.setPauseStartDate(null);

        return membershipRepository.save(membership);
    }
}