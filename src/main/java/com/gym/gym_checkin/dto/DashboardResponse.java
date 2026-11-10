
package com.gym.gym_checkin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DashboardResponse {

    private long totalMembers;

    private long activeMemberships;

    private long todayCheckins;

    private long lowCreditMembers;

    private List<AttendanceTrend> attendanceTrend;

    private List<TodayCheckin> todayCheckinsList;

    private List<MembershipAlert> membershipAlerts;

    @Getter
    @Setter
    public static class AttendanceTrend {

        private LocalDate date;

        private long checkins;
    }

    @Getter
    @Setter
    public static class TodayCheckin {

        private Long id;

        private String memberName;

        private String memberType;

        private String sessionType;

        private String classTiming;

        private LocalDateTime checkinTime;
    }

    @Getter
    @Setter
    public static class MembershipAlert {

        private Long membershipId;

        private Long memberId;

        private String memberName;

        private String packageName;

        private Integer remainingCredits;

        private LocalDate expiryDate;
    }
}
