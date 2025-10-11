package com.example.link.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID fileId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private boolean isPublic = true;

    private LocalDateTime expiresAt;

    private Integer downloadCount;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
