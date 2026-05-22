package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MembershipResponse {

    private String packageName;

    private Integer remainingCredits;

    private LocalDate expiryDate;
}
