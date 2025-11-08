package com.example.file.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.file.model.entity.FileVariant;

public interface FileVariantRepository extends JpaRepository<FileVariant, UUID> {
    List<FileVariant> findByFile_Id(UUID fileId);
}
