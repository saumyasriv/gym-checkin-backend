package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.*;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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

                        membershipResponse.setPackageName(
                                membership.getPackageName()
                        );

                        membershipResponse.setRemainingCredits(
                                membership.getRemainingCredits() != null
                                        ? membership.getRemainingCredits()
                                        : 0
                        );

                        membershipResponse.setExpiryDate(
                                membership.getExpiryDate()
                        );

                        return membershipResponse;

                    }).toList();

            response.setMemberships(membershipResponses);

            int totalCredits = member.getMemberships()
                    .stream()
                    .mapToInt(m ->
                            m.getRemainingCredits() != null
                                    ? m.getRemainingCredits()
                                    : 0
                    )
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

            int totalCredits = member.getMemberships()
                    .stream()
                    .mapToInt(m ->
                            m.getRemainingCredits() != null
                                    ? m.getRemainingCredits()
                                    : 0
                    )
                    .sum();

            response.setTotalRemainingCredits(totalCredits);

            return response;

        }).toList();
    }

    public MemberDetailsResponse getMemberDetails(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MemberDetailsResponse response =
                new MemberDetailsResponse();

        response.setId(member.getId());
        response.setName(member.getName());
        response.setPhone(member.getPhone());
        response.setEmail(member.getEmail());
        response.setNotes(member.getNotes());

        int totalCredits = member.getMemberships()
                .stream()
                .mapToInt(m ->
                        m.getRemainingCredits() != null
                                ? m.getRemainingCredits()
                                : 0
                )
                .sum();

        response.setTotalRemainingCredits(totalCredits);

        List<MembershipResponse> memberships =
                member.getMemberships().stream().map(membership -> {

                    MembershipResponse membershipResponse =
                            new MembershipResponse();

                    membershipResponse.setPackageName(
                            membership.getPackageName()
                    );

                    membershipResponse.setRemainingCredits(
                            membership.getRemainingCredits() != null
                                    ? membership.getRemainingCredits()
                                    : 0
                    );

                    membershipResponse.setExpiryDate(
                            membership.getExpiryDate()
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

                            return checkinResponse;

                        }).toList();

        response.setCheckins(checkins);

        return response;
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public void deleteAllMembers() {
        memberRepository.deleteAll();
    }
}