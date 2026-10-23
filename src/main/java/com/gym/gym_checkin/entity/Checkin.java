package com.gym.gym_checkin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "checkins")
@Getter
@Setter
public class Checkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime checkinTime;

    private String type;

    private String classTiming;

    private boolean backdated = false;

    private String enteredBy;

    private LocalDateTime enteredAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "membership_id")
    private Membership membership;

    @PrePersist
    public void prePersist() {
        if (this.checkinTime == null) {
            this.checkinTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
        if (this.enteredAt == null) {
            this.enteredAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
    }
}