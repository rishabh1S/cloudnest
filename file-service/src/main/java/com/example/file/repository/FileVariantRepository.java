package com.example.file.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.file.model.entity.FileVariant;

@Repository
public interface FileVariantRepository extends JpaRepository<FileVariant, UUID> {

    List<FileVariant> findByFileId(UUID fileId);
}
