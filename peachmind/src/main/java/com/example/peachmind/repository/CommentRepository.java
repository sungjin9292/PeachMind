package com.example.peachmind.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.peachmind.entity.CommentEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
	
    List<CommentEntity> findByPostId(Long postId); // 게시글 ID에 해당하는 댓글들을 찾는 쿼리
    
    public List<CommentEntity> findByParentComment(CommentEntity parentComment);

}