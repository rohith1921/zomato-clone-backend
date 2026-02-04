package com.food.ordering.system.infra.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass // This tells JPA: "Don't make a table for this, but include these columns in child tables"
@EntityListeners(AuditingEntityListener.class) // Auto-populates timestamps
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // UUIDs are better for distributed systems than Long (1, 2, 3)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Version for Optimistic Locking (Prevent lost updates without DB locks)
    @Version
    private Long version;
}