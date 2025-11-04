package com.example.file.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.file.model.entity.FileMetadata;
import com.example.file.model.entity.User;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    List<FileMetadata> findByOwner(User owner);
    String findFilenameById(UUID id);
}
