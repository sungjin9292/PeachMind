package com.example.peachmind.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.peachmind.entity.PostEntity;
import org.springframework.data.domain.Page;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    // 필요한 커스텀 메서드도 추가 가능
	
	// author(사용자) 기준으로 게시글을 조회하는 메서드
    //List<PostEntity> findByAuthor(String author);
    
    // createdDate 기준으로 내림차순 정렬
    List<PostEntity> findAllByOrderByCreatedDateDesc();
    
    // username을 기준으로 페이징 처리하여 게시글을 가져오는 메서드
    Page<PostEntity> findByAuthor(String author, Pageable pageable);
    
}
