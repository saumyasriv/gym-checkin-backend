package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberResponse {

    private Long id;

    private String name;

    private String phone;

    private Integer totalRemainingCredits;

    private List<MembershipResponse> memberships;
}
