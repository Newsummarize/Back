package com.newsummarize.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Table(name = "interest")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestId;

    @Column(name = "interest_category", nullable = false, unique = true)
    private String interestCategory;
}
