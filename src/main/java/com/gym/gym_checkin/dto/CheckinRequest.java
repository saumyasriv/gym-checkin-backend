package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckinRequest {

    private Long memberId;
    private String type;
    private String classTiming;
}