package com.example.peachmind.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;  // 파일 이름
    private String filePath;  // 파일 경로
    private String fileType;  // 파일 타입
    private Long fileSize;    // 파일 크기

    // 파일과 게시글 Many-to-One 관계
    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity post;
    
}
