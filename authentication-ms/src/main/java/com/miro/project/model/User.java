package com.miro.project.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "role"}),
        @UniqueConstraint(columnNames = {"email", "role"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    // @Enumerated(EnumType.STRING) so it saves "PATIENT" in the database instead of an integer.
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.PATIENT;
}