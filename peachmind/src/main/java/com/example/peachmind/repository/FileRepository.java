package com.example.peachmind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.peachmind.entity.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
	
}
