package com.gym.gym_checkin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Table(name = "member")
@Setter
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phone;

    private String email;

    private String notes;

    @OneToMany(mappedBy = "member")
    private List<Membership> memberships;

    @OneToMany(mappedBy = "member")
    private List<Checkin> checkins;

    // getters setters
}