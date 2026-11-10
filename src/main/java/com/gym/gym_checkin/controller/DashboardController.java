
package com.gym.gym_checkin.controller;

import com.gym.gym_checkin.dto.DashboardResponse;
import com.gym.gym_checkin.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://gym-checkin-frontend.onrender.com"
})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboardData();
    }
}
