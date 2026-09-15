package com.gym.gym_checkin.repository;

import com.gym.gym_checkin.entity.MembershipPauseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPauseHistoryRepository
        extends JpaRepository<MembershipPauseHistory, Long> {
}