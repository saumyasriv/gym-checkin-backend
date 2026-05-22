package com.gym.gym_checkin.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberDetailsResponse {

    private Long id;

    private String name;

    private String phone;

    private String email;

    private String notes;

    private Integer totalRemainingCredits;

    private List<MembershipResponse> memberships;

    private List<CheckinResponse> checkins;
}
