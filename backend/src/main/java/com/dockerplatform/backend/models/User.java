package com.dockerplatform.backend.models;

import java.util.Set;
import java.util.UUID;

import com.dockerplatform.backend.models.enums.BadgeType;
import com.dockerplatform.backend.models.enums.UserRole;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username; 

    @Column(nullable = false)
    private String password; 

    @Column(unique = true, nullable = false)
    private String email; 

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role; 

    @Enumerated(EnumType.STRING)
    private BadgeType badge;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private Set<Repository> repositories;
}
