
package com.gym.gym_checkin.repository;

import com.gym.gym_checkin.entity.Checkin;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    @Modifying
    @Transactional
    @Query("delete from Checkin c where c.member.id = :memberId")
    void deleteAllByMemberId(Long memberId);

    List<Checkin> findByTypeAndCheckinTimeGreaterThanEqualAndCheckinTimeLessThanOrderByCheckinTimeDesc(
            String type,
            LocalDateTime start,
            LocalDateTime end
    );
}
