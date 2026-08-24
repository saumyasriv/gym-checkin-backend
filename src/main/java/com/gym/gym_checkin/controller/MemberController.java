package com.gym.gym_checkin.controller;

import com.gym.gym_checkin.dto.MemberDetailsResponse;
import com.gym.gym_checkin.dto.MemberResponse;
import com.gym.gym_checkin.dto.MemberSearchResponse;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://gym-checkin-frontend.onrender.com"
})
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public Member createMember(@RequestBody Member member) {
        return memberService.createMember(member);
    }

    @GetMapping
    public List<MemberResponse> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/search")
    public List<MemberSearchResponse> searchMembers(
            @RequestParam String query
    ) {
        return memberService.searchMembers(query);
    }

    @GetMapping("/{id}")
    public MemberDetailsResponse getMemberDetails(
            @PathVariable Long id
    ) {
        return memberService.getMemberDetails(id);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
    }

    @DeleteMapping
    public void deleteAllMembers() {
        memberService.deleteAllMembers();
    }

    @PutMapping("/{id}/notes")
    public Member updateNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return memberService.updateNotes(
                id,
                body.get("notes")
        );
    }
}
