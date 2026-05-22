package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchResponse {

    private Long id;

    private String name;

    private Integer totalRemainingCredits;
}
