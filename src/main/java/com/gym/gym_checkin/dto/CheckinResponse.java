package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CheckinResponse {

    private LocalDateTime checkinTime;
    private String type;
    private String classTiming;
    private Long membershipId;

}