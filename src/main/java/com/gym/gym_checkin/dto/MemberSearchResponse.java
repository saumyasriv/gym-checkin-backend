package com.gym.gym_checkin.dto;

import com.gym.gym_checkin.entity.MemberType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchResponse {

    private Long id;

    private String name;

    private MemberType memberType;

    private Integer totalRemainingCredits;
}