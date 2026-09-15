package com.gym.gym_checkin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "membership")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String packageName;

    private Integer totalCredits;

    private Integer remainingCredits;

    private Double amountPaid;

    private LocalDate startDate;

    private LocalDate expiryDate;

    private boolean paused;

    private LocalDate pauseStartDate;

    @ManyToOne
    @JsonIgnore
    private Member member;

    @OneToMany(
            mappedBy = "membership",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("pauseStartDate DESC")
    private List<MembershipPauseHistory> pauseHistory =
            new ArrayList<>();
}