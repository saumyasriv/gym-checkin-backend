package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.CheckinRequest;
import com.gym.gym_checkin.entity.Checkin;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.repository.CheckinRepository;
import com.gym.gym_checkin.repository.MemberRepository;
import com.gym.gym_checkin.repository.MembershipRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CheckinService {

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final CheckinRepository checkinRepository;

    public CheckinService(MemberRepository memberRepository,
                          MembershipRepository membershipRepository,
                          CheckinRepository checkinRepository) {
        this.memberRepository = memberRepository;
        this.membershipRepository = membershipRepository;
        this.checkinRepository = checkinRepository;
    }

    public String processAttendance(CheckinRequest request, String type) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Membership> memberships =
                membershipRepository
                        .findByMemberAndRemainingCreditsGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                                member,
                                0,
                                LocalDate.now()
                        );

        if (memberships.isEmpty()) {
            throw new RuntimeException("No active memberships found");
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
