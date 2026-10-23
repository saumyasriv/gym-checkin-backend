package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.*;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.entity.MembershipPauseHistory;
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

            // Member classification
            response.setMemberType(member.getMemberType());

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

                        // Expiry removal information
                        membershipResponse.setExpiryRemoved(
                                membership.isExpiryRemoved()
                        );

                        membershipResponse.setExpiryRemovedDate(
                                membership.getExpiryRemovedDate()
                        );

                        // Current pause information
                        membershipResponse.setPaused(
                                membership.isPaused()
                        );

                        membershipResponse.setPauseStartDate(
                                membership.getPauseStartDate()
                        );

                        // Pause history
                        membershipResponse.setPauseHistory(
                                membership.getPauseHistory()
                                        .stream()
                                        .map(this::toPauseHistoryResponse)
                                        .toList()
                        );

                        return membershipResponse;

                    }).toList();

            response.setMemberships(membershipResponses);

            /*
             * TOTAL REMAINING CREDITS
             *
             * Only include credits from ACTIVE memberships.
             *
             * Active means:
             * - Not paused
             * - More than 0 remaining credits
             * - Not expired
             * - OR expiry has been removed
             */
            int totalCredits = member.getMemberships()
                    .stream()
                    .filter(this::isActiveMembership)
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

            // Member classification
            response.setMemberType(member.getMemberType());

            /*
             * Include paused memberships in the total remaining
             * credits shown in search results.
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

        // Member classification
        response.setMemberType(member.getMemberType());

        /*
         * TOTAL REMAINING CREDITS
         *
         * Only include credits from ACTIVE memberships.
         *
         * Paused, expired, and zero-credit memberships
         * are not included.
         */
        int totalCredits = member.getMemberships()
                .stream()
                .filter(this::isActiveMembership)
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

                    // Expiry removal information
                    membershipResponse.setExpiryRemoved(
                            membership.isExpiryRemoved()
                    );

                    membershipResponse.setExpiryRemovedDate(
                            membership.getExpiryRemovedDate()
                    );

                    // Current pause information
                    membershipResponse.setPaused(
                            membership.isPaused()
                    );

                    membershipResponse.setPauseStartDate(
                            membership.getPauseStartDate()
                    );

                    // Pause history
                    membershipResponse.setPauseHistory(
                            membership.getPauseHistory()
                                    .stream()
                                    .map(this::toPauseHistoryResponse)
                                    .toList()
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
                            checkinResponse.setBackdated(checkin.isBackdated());
                            checkinResponse.setEnteredBy(checkin.getEnteredBy());
                            checkinResponse.setEnteredAt(checkin.getEnteredAt());
                            checkinResponse.setExpiredMembership(
                                    checkin.getMembership() != null
                                            && !checkin.getMembership().isExpiryRemoved()
                                            && checkin.getMembership().getExpiryDate() != null
                                            && checkin.getMembership().getExpiryDate().isBefore(LocalDate.now())
                            );

                            return checkinResponse;

                        }).toList();

        response.setCheckins(checkins);

        return response;
    }

    /*
     * Convert the database pause-history entity into the
     * lightweight DTO returned to the frontend.
     */
    private MembershipPauseHistoryResponse toPauseHistoryResponse(
            MembershipPauseHistory history
    ) {

        MembershipPauseHistoryResponse response =
                new MembershipPauseHistoryResponse();

        response.setId(history.getId());

        response.setPauseStartDate(
                history.getPauseStartDate()
        );

        response.setPauseEndDate(
                history.getPauseEndDate()
        );

        response.setPausedDays(
                history.getPausedDays()
        );

        response.setDaysAddedToExpiry(
                history.getDaysAddedToExpiry()
        );

        return response;
    }

    /*
     * ACTIVE MEMBERSHIP
     *
     * A membership is active when:
     *
     * 1. It is not paused.
     * 2. It has more than 0 remaining credits.
     * 3. Its expiry has not passed.
     *
     * If expiry has been removed, the membership remains
     * active indefinitely as long as it is not paused and
     * still has credits.
     */
    private boolean isActiveMembership(Membership membership) {

        if (membership.isPaused()) {
            return false;
        }

        if (membership.getRemainingCredits() == null ||
                membership.getRemainingCredits() <= 0) {
            return false;
        }

        if (membership.isExpiryRemoved()) {
            return true;
        }

        return membership.getExpiryDate() != null &&
                !membership.getExpiryDate().isBefore(LocalDate.now());
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

        member.setNotes(member.getNotes());

        return memberRepository.save(member);
    }
}