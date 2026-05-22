package com.gym.gym_checkin.repository;

import com.gym.gym_checkin.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    void deleteAllByMemberId(Long memberId);

}