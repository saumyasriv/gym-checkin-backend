package com.gym.gym_checkin.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateMembershipRequest {

    private Long memberId;

    private String packageName;

    private Integer totalCredits;

    private Integer remainingCredits;

    private Double amountPaid;

    private LocalDate startDate;

    private LocalDate expiryDate;
}
