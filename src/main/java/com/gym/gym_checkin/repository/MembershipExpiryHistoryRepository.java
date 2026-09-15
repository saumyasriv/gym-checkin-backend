package com.gym.gym_checkin.repository;

import com.gym.gym_checkin.entity.MembershipExpiryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipExpiryHistoryRepository
        extends JpaRepository<MembershipExpiryHistory, Long> {
}