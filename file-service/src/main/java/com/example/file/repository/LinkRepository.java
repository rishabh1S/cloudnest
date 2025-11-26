package com.example.file.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.file.model.entity.Link;

@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {
    Link findByFileId(UUID fileId);
    Optional<Link> findByToken(String token);
}
