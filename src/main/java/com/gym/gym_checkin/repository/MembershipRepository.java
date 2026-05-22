package com.gym.gym_checkin.repository;

import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByMemberAndRemainingCreditsGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
            Member member,
            Integer remainingCredits,
            LocalDate expiryDate
    );

    @Modifying
    @Transactional
    @Query("delete from Membership m where m.member.id = :memberId")
    void deleteAllByMemberId(Long memberId);
}