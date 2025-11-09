package com.example.file.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.file.model.entity.FileMetadata;
import com.example.file.model.entity.User;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    @Query("SELECT f.filename FROM FileMetadata f WHERE f.id = :id")
    String findFilenameById(@Param("id") UUID id);
    List<FileMetadata> findByOwner(User owner);
    Optional<FileMetadata> findByObjectKey(String objectKey);
}
