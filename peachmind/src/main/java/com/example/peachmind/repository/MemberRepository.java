package com.example.peachmind.repository;

import com.example.peachmind.entity.MemberEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
	Optional<MemberEntity> findByUsername(String username);  // 사용자명으로 사용자 조회
}