package com.gym.gym_checkin.controller;

import com.gym.gym_checkin.dto.CreateMembershipRequest;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.service.MembershipService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/memberships")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://gym-checkin-frontend.onrender.com"
})
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public Membership createMembership(
            @RequestBody CreateMembershipRequest request
    ) {
        return membershipService.createMembership(request);
    }

    @PostMapping("/{membershipId}/pause")
    public Membership pauseMembership(
            @PathVariable Long membershipId
    ) {
        return membershipService.pauseMembership(membershipId);
    }

    @PostMapping("/{membershipId}/unpause")
    public Membership unpauseMembership(
            @PathVariable Long membershipId
    ) {
        return membershipService.unpauseMembership(membershipId);
    }

    @PostMapping("/{membershipId}/remove-expiry")
    public Membership removeExpiry(
            @PathVariable Long membershipId
    ) {
        return membershipService.removeExpiry(membershipId);
    }

    @PostMapping("/{membershipId}/restore-expiry")
    public Membership restoreExpiry(
            @PathVariable Long membershipId
    ) {
        return membershipService.restoreExpiry(membershipId);
    }
}