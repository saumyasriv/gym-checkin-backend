package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.*;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.repository.CheckinRepository;
import com.gym.gym_checkin.repository.MemberRepository;
import com.gym.gym_checkin.repository.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final CheckinRepository checkinRepository;

    public MemberService(
            MemberRepository memberRepository,
            MembershipRepository membershipRepository,
            CheckinRepository checkinRepository
    ) {
        this.memberRepository = memberRepository;
        this.membershipRepository = membershipRepository;
        this.checkinRepository = checkinRepository;
    }

    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    public List<MemberResponse> getAllMembers() {

        List<Member> members = memberRepository.findAll();

        return members.stream().map(member -> {

            MemberResponse response = new MemberResponse();

            response.setId(member.getId());
            response.setName(member.getName());
            response.setPhone(member.getPhone());

            List<MembershipResponse> membershipResponses =
                    member.getMemberships().stream().map(membership -> {

                        MembershipResponse membershipResponse =
                                new MembershipResponse();

                        // Membership ID
                        membershipResponse.setId(
                                membership.getId()
                        );

                        membershipResponse.setPackageName(
                                membership.getPackageName()
                        );

                        membershipResponse.setRemainingCredits(
                                membership.getRemainingCredits() != null
                                        ? membership.getRemainingCredits()
                                        : 0
                        );

                        membershipResponse.setStartDate(
                                membership.getStartDate()
                        );

                        membershipResponse.setExpiryDate(
                                membership.getExpiryDate()
                        );

                        // Pause information
                        membershipResponse.setPaused(
                                membership.isPaused()
                        );

                        membershipResponse.setPauseStartDate(
                                membership.getPauseStartDate()
                        );

                        return membershipResponse;

                    }).toList();

            response.setMemberships(membershipResponses);

            /*
             * TOTAL REMAINING CREDITS
             *
             * Include ALL memberships here, including paused
             * memberships.
             *
             * A paused membership still has valid remaining
             * credits. Pausing only prevents those credits from
             * being used until the membership is unpaused.
             */
            int totalCredits = member.getMemberships()
                    .stream()
                    .filter(membership ->
                            membership.getRemainingCredits() != null
                    )
                    .mapToInt(Membership::getRemainingCredits)
                    .sum();

            response.setTotalRemainingCredits(totalCredits);

            return response;

        }).toList();
    }

    public List<MemberSearchResponse> searchMembers(String query) {

        List<Member> members =
                memberRepository.findByNameContainingIgnoreCase(query);

        return members.stream().map(member -> {

            MemberSearchResponse response =
                    new MemberSearchResponse();

            response.setId(member.getId());
            response.setName(member.getName());

            /*
             * Include paused memberships in the total remaining
             * credits shown in search results as well.
             */
            int totalCredits = member.getMemberships()
                    .stream()
                    .filter(membership ->
                            membership.getRemainingCredits() != null
                    )
                    .mapToInt(Membership::getRemainingCredits)
                    .sum();

            response.setTotalRemainingCredits(totalCredits);

            return response;

        }).toList();
    }

    public MemberDetailsResponse getMemberDetails(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new RuntimeException("Member not found")
                );

        MemberDetailsResponse response =
                new MemberDetailsResponse();

        response.setId(member.getId());
        response.setName(member.getName());
        response.setPhone(member.getPhone());
        response.setEmail(member.getEmail());
        response.setNotes(member.getNotes());

        /*
         * TOTAL REMAINING CREDITS
         *
         * Include paused memberships.
         *
         * Example:
         *
         * Active membership  = 10 credits
         * Paused membership  = 23 credits
         * Total shown        = 33 credits
         *
         * The paused 23 credits are still the member's credits;
         * they are simply unavailable for check-in while paused.
         */
        int totalCredits = member.getMemberships()
                .stream()
                .filter(membership ->
                        membership.getRemainingCredits() != null
                )
                .mapToInt(Membership::getRemainingCredits)
                .sum();

        response.setTotalRemainingCredits(totalCredits);

        List<MembershipResponse> memberships =
                member.getMemberships().stream().map(membership -> {

                    MembershipResponse membershipResponse =
                            new MembershipResponse();

                    // Membership ID
                    membershipResponse.setId(
                            membership.getId()
                    );

                    membershipResponse.setPackageName(
                            membership.getPackageName()
                    );

                    membershipResponse.setRemainingCredits(
                            membership.getRemainingCredits() != null
                                    ? membership.getRemainingCredits()
                                    : 0
                    );

                    membershipResponse.setStartDate(
                            membership.getStartDate()
                    );

                    membershipResponse.setExpiryDate(
                            membership.getExpiryDate()
                    );

                    // Pause information
                    membershipResponse.setPaused(
                            membership.isPaused()
                    );

                    membershipResponse.setPauseStartDate(
                            membership.getPauseStartDate()
                    );

                    return membershipResponse;

                }).toList();

        response.setMemberships(memberships);

        List<CheckinResponse> checkins =
                member.getCheckins().stream()
                        .sorted(
                                (a, b) -> b.getCheckinTime()
                                        .compareTo(a.getCheckinTime())
                        )
                        .map(checkin -> {

                            CheckinResponse checkinResponse =
                                    new CheckinResponse();

                            checkinResponse.setCheckinTime(
                                    checkin.getCheckinTime()
                            );

                            checkinResponse.setType(
                                    checkin.getType()
                            );

                            checkinResponse.setClassTiming(
                                    checkin.getClassTiming()
                            );

                            // Tell the frontend which membership
                            // this check-in belongs to.
                            checkinResponse.setMembershipId(
                                    checkin.getMembership() != null
                                            ? checkin.getMembership().getId()
                                            : null
                            );

                            return checkinResponse;

                        }).toList();

        response.setCheckins(checkins);

        return response;
    }

    /*
     * ACTIVE MEMBERSHIP
     *
     * Used specifically for determining whether a membership
     * can currently be used for check-in.
     *
     * A paused membership is NOT active.
     */
    private boolean isActiveMembership(Membership membership) {

        return !membership.isPaused()
                && membership.getRemainingCredits() != null
                && membership.getRemainingCredits() > 0
                && membership.getExpiryDate() != null
                && !membership.getExpiryDate().isBefore(LocalDate.now());
    }

    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllMembers() {

        checkinRepository.deleteAll();
        membershipRepository.deleteAll();
        memberRepository.deleteAll();
    }

    public Member updateNotes(Long memberId, String notes) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new RuntimeException("Member not found")
                );

        member.setNotes(notes);

        return memberRepository.save(member);
    }
}