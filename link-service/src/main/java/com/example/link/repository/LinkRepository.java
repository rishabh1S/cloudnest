package com.example.link.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.link.model.entity.Link;

public interface LinkRepository extends JpaRepository<Link, UUID> {
    Optional<Link> findByToken(String token);
}
