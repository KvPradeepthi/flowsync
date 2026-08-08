package com.flowsync.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * User entity — supports CUSTOMER and ADMIN roles.
 * Password is stored as BCrypt hash (never plain text).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;   // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    public enum Role {
        CUSTOMER,
        ADMIN
    }
}
