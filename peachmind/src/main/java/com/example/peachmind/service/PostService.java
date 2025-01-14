package com.example.peachmind.service;

import java.io.File;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.peachmind.entity.FileEntity;
import com.example.peachmind.entity.PostEntity;
import com.example.peachmind.repository.FileRepository;
import com.example.peachmind.repository.PostRepository;

@Service
public class PostService {

	@Autowired
	private PostRepository postRepository;
	
	@Autowired
    private FileRepository fileRepository;

	public List<PostEntity> getPostsSorted(String username) {
		return postRepository.findAllByOrderByCreatedDateDesc(); // 게시글을 내림차순으로 가져옴
	}

	// 게시글을 저장하는 메서드
	public void savePost(PostEntity post) {
		postRepository.save(post);
	}

	// 게시글 상세 조회
	public PostEntity getPostById(Long id) {
		return postRepository.findById(id).orElse(null);
	}

	// 게시글 삭제 메서드
	public void deletePostById(Long id) {
		postRepository.deleteById(id);
	}
	
	public Page<PostEntity> findAllPostsWithPagination(Pageable pageable) {
	    Pageable sortedByDateDesc = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());
	    return postRepository.findAll(sortedByDateDesc);  // 내림차순으로 정렬된 게시글을 페이지네이션하여 가져옴
	}

	// 페이지 조회 메서드 (사용자 기준으로 페이징 처리)
    public Page<PostEntity> findPostsWithPagination(String username, Pageable pageable) {
        return postRepository.findByAuthor(username, pageable); // 사용자 기준으로 페이징 처리
    }
    
    // 파일 삭제 메서드
    public void deleteFileById(Long fileId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        // 파일 경로 출력
        System.out.println("파일 경로: " + fileEntity.getFilePath());

        File file = new File(fileEntity.getFilePath());
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new RuntimeException("파일 삭제에 실패했습니다.");
            }
        } else {
            throw new RuntimeException("파일이 존재하지 않습니다.");
        }

        // DB에서 파일 엔티티 삭제
        fileRepository.delete(fileEntity);
    }


}
