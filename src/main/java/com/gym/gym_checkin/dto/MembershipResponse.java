package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MembershipResponse {

    private Long id;
    private String packageName;
    private Integer remainingCredits;
    private LocalDate startDate;
    private LocalDate expiryDate;

    private boolean paused;
    private LocalDate pauseStartDate;
}