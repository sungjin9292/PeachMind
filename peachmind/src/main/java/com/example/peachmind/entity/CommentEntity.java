package com.example.peachmind.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author; // 댓글 작성자
    private String content; // 댓글 내용
    private LocalDateTime createdDate = LocalDateTime.now(); // 작성일

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private CommentEntity parentComment; // 부모 댓글

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true) // parentComment 기준으로 매핑
    private List<CommentEntity> replies = new ArrayList<>(); // 자식 댓글

    // 부모 댓글이 있을 경우, 자식 댓글 추가
    public void addReply(CommentEntity reply) {
        replies.add(reply);
        reply.setParentComment(this);
    }

    // 부모 댓글이 있을 경우, 자식 댓글 제거
    public void removeReply(CommentEntity reply) {
        replies.remove(reply);
        reply.setParentComment(null);
    }
}
