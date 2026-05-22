package com.gym.gym_checkin.controller;

import com.gym.gym_checkin.dto.CheckinRequest;
import com.gym.gym_checkin.service.CheckinService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkins")
@CrossOrigin(origins = "http://localhost:5173")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping
    public String checkin(@RequestBody CheckinRequest request) {
        return checkinService.processAttendance(request, request.getType());
    }
}
