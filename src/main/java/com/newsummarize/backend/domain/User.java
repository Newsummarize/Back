package com.newsummarize.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")          // user_id
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;            // user_name

    @Column(nullable = true)
    private String age;                // age

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;              // M, F, Other

    @Column(nullable = false, unique = true)
    private String email;               // email

    @Column(nullable = false)
    private String password;            // BCrypt 해시 저장

    @ManyToMany
    @JoinTable(
            name = "user_interest",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )

    private Set<Interest> interests = new HashSet<>();

}
