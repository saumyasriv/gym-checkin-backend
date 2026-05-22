package com.gym.gym_checkin.repository;

import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByMemberAndRemainingCreditsGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
            Member member,
            Integer remainingCredits,
            LocalDate expiryDate
    );

    void deleteAllByMemberId(Long memberId);

}