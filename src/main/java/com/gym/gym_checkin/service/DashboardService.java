
package com.gym.gym_checkin.service;

import com.gym.gym_checkin.dto.DashboardResponse;
import com.gym.gym_checkin.entity.Checkin;
import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.repository.CheckinRepository;
import com.gym.gym_checkin.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final ZoneId ZONE =
            ZoneId.of("Asia/Kolkata");

    private final MemberRepository memberRepository;
    private final CheckinRepository checkinRepository;

    public DashboardService(
            MemberRepository memberRepository,
            CheckinRepository checkinRepository
    ) {
        this.memberRepository = memberRepository;
        this.checkinRepository = checkinRepository;
    }

    public DashboardResponse getDashboardData() {

        LocalDate today = LocalDate.now(ZONE);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate alertEndDate = today.plusDays(7);

        LocalDateTime startDateTime =
                today.minusDays(6).atStartOfDay();

        LocalDateTime endDateTime =
                tomorrow.atStartOfDay();

        List<Member> members = memberRepository.findAll();

        List<Checkin> checkins =
                checkinRepository
                        .findByTypeAndCheckinTimeGreaterThanEqualAndCheckinTimeLessThanOrderByCheckinTimeDesc(
                                "CHECKIN",
                                startDateTime,
                                endDateTime
                        );

        long activeMembershipCount = 0;
        long lowCreditMemberCount = 0;

        List<DashboardResponse.MembershipAlert> alerts =
                new ArrayList<>();

        for (Member member : members) {

            int activeCredits = 0;

            if (member.getMemberships() == null) {
                continue;
            }

            for (Membership membership : member.getMemberships()) {

                if (isActiveMembership(membership, today)) {

                    activeMembershipCount++;

                    activeCredits +=
                            membership.getRemainingCredits();

                    LocalDate expiryDate =
                            membership.getExpiryDate();

                    if (expiryDate != null
                            && !expiryDate.isBefore(today)
                            && !expiryDate.isAfter(alertEndDate)) {

                        DashboardResponse.MembershipAlert alert =
                                new DashboardResponse.MembershipAlert();

                        alert.setMembershipId(membership.getId());
                        alert.setMemberId(member.getId());
                        alert.setMemberName(member.getName());
                        alert.setPackageName(membership.getPackageName());
                        alert.setRemainingCredits(
                                membership.getRemainingCredits()
                        );
                        alert.setExpiryDate(expiryDate);

                        alerts.add(alert);
                    }
                }
            }

            if (activeCredits >= 1 && activeCredits <= 3) {
                lowCreditMemberCount++;
            }
        }

        alerts.sort(
                Comparator.comparing(
                        DashboardResponse.MembershipAlert::getExpiryDate
                )
        );

        Map<LocalDate, Long> dailyCheckins = new HashMap<>();

        List<DashboardResponse.AttendanceTrend> attendanceTrend =
                new ArrayList<>();

        for (int i = 6; i >= 0; i--) {

            LocalDate date = today.minusDays(i);

            dailyCheckins.put(date, 0L);

            DashboardResponse.AttendanceTrend trend =
                    new DashboardResponse.AttendanceTrend();

            trend.setDate(date);
            trend.setCheckins(0);

            attendanceTrend.add(trend);
        }

        List<DashboardResponse.TodayCheckin> todayCheckinsList =
                new ArrayList<>();

        long todayCheckinCount = 0;

        for (Checkin checkin : checkins) {

            if (checkin.getCheckinTime() == null) {
                continue;
            }

            LocalDate checkinDate =
                    checkin.getCheckinTime().toLocalDate();

            dailyCheckins.merge(checkinDate, 1L, Long::sum);

            if (checkinDate.equals(today)) {

                todayCheckinCount++;

                Member member = checkin.getMember();

                if (member == null) {
                    continue;
                }

                DashboardResponse.TodayCheckin todayCheckin =
                        new DashboardResponse.TodayCheckin();

                todayCheckin.setId(checkin.getId());
                todayCheckin.setMemberName(member.getName());

                String memberType =
                        member.getMemberType() != null
                                ? member.getMemberType().name()
                                : null;

                todayCheckin.setMemberType(memberType);

                todayCheckin.setSessionType(
                        "GROUP".equals(memberType)
                                ? "Group Class"
                                : "PT"
                );

                todayCheckin.setClassTiming(checkin.getClassTiming());
                todayCheckin.setCheckinTime(checkin.getCheckinTime());

                todayCheckinsList.add(todayCheckin);
            }
        }

        for (DashboardResponse.AttendanceTrend trend : attendanceTrend) {

            trend.setCheckins(
                    dailyCheckins.getOrDefault(trend.getDate(), 0L)
            );
        }

        DashboardResponse response = new DashboardResponse();

        response.setTotalMembers(members.size());
        response.setActiveMemberships(activeMembershipCount);
        response.setTodayCheckins(todayCheckinCount);
        response.setLowCreditMembers(lowCreditMemberCount);
        response.setAttendanceTrend(attendanceTrend);
        response.setTodayCheckinsList(todayCheckinsList);
        response.setMembershipAlerts(alerts);

        return response;
    }

    private boolean isActiveMembership(
            Membership membership,
            LocalDate today
    ) {

        if (membership.isPaused()) {
            return false;
        }

        if (membership.getRemainingCredits() == null
                || membership.getRemainingCredits() <= 0) {
            return false;
        }

        if (membership.isExpiryRemoved()) {
            return true;
        }

        return membership.getExpiryDate() != null
                && !membership.getExpiryDate().isBefore(today);
    }
}
