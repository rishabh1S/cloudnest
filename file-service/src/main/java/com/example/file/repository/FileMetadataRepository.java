package com.example.file.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.file.model.FileMetadata;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    void deleteByFilename(String filename);
}
