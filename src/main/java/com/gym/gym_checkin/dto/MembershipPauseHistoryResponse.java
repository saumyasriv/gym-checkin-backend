package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MembershipPauseHistoryResponse {

    private Long id;

    private LocalDate pauseStartDate;

    private LocalDate pauseEndDate;

    private Integer pausedDays;

    private Integer daysAddedToExpiry;
}