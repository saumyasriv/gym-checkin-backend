package com.gym.gym_checkin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    
    @ManyToOne
    @JoinColumn(name = "membership_id")
    private Membership membership;

    @PrePersist
    public void prePersist() {
        this.checkinTime = LocalDateTime.now();
    }
}
