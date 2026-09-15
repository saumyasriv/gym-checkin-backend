package com.gym.gym_checkin.dto;

import com.gym.gym_checkin.entity.MemberType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberResponse {

    private Long id;

    private String name;

    private String phone;

    private MemberType memberType;

    private Integer totalRemainingCredits;

    private List<MembershipResponse> memberships;
}