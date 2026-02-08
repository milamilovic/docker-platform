package com.dockerplatform.backend.models;

import java.util.Set;
import java.util.UUID;

import com.dockerplatform.backend.models.enums.BadgeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"owner_id", "name"})
        }
)
public class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String ownerUsername;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private Long createdAt;

    private Long modifiedAt;

    private int numberOfPulls;
    private int numberOfStars;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private boolean isOfficial;

    @Enumerated(EnumType.STRING)
    private BadgeType badge;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tag> tags;
}
