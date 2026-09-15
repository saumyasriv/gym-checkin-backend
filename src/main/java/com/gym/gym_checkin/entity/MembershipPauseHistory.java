package com.gym.gym_checkin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "membership_pause_history")
public class MembershipPauseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate pauseStartDate;

    private LocalDate pauseEndDate;

    private Integer pausedDays;

    private Integer daysAddedToExpiry;

    @ManyToOne
    @JoinColumn(name = "membership_id", nullable = false)
    @JsonIgnore
    private Membership membership;
}