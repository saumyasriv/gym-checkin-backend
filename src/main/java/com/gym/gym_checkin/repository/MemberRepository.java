package com.gym.gym_checkin.repository;

import com.gym.gym_checkin.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long id);
    List<Member> findByNameContainingIgnoreCase(String name);
}
