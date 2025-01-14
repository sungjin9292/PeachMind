package com.example.peachmind.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.peachmind.entity.CommentEntity;
import com.example.peachmind.repository.CommentRepository;

import java.util.List;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;

    // 댓글 저장
    @Transactional
    public void saveComment(CommentEntity comment) {
        commentRepository.save(comment);
    }

    // 댓글 조회
    public CommentEntity getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
    
}
