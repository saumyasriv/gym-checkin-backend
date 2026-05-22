package com.gym.gym_checkin.controller;

import com.gym.gym_checkin.dto.CreateMembershipRequest;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.service.MembershipService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/memberships")
@CrossOrigin(origins = "http://localhost:5173")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public Membership createMembership(@RequestBody CreateMembershipRequest request) {
        return membershipService.createMembership(request);
    }
}
