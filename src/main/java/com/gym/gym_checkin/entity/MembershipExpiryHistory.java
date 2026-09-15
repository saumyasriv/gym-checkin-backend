package com.gym.gym_checkin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "membership_expiry_history")
public class MembershipExpiryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate expiryRemovedDate;

    private LocalDate expiryRestoredDate;

    private Integer daysWithoutExpiry;

    private Integer daysAddedToExpiry;

    @ManyToOne
    @JoinColumn(name = "membership_id", nullable = false)
    @JsonIgnore
    private Membership membership;
}