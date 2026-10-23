package com.gym.gym_checkin.controller;

import com.gym.gym_checkin.dto.CheckinRequest;
import com.gym.gym_checkin.service.CheckinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/checkins")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://gym-checkin-frontend.onrender.com"
})
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/backdated")
    public ResponseEntity<String> backdatedCheckin(
            @RequestBody CheckinRequest request,
            @RequestHeader(value = "X-Admin-User", required = false) String adminUser
    ) {
        try {
            return ResponseEntity.ok(checkinService.processBackdatedAttendance(request, adminUser));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping
    public ResponseEntity<String> checkin(
            @RequestBody CheckinRequest request
    ) {

        try {

            String response = checkinService.processAttendance(
                    request,
                    request.getType()
            );

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
}