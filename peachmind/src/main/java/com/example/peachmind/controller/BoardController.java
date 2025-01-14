package com.example.peachmind.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.peachmind.entity.CommentEntity;
import com.example.peachmind.entity.FileEntity;
import com.example.peachmind.entity.PostEntity;
import com.example.peachmind.service.CommentService;
import com.example.peachmind.service.PostService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/peachmind/board")
public class BoardController {

	@Autowired
	private PostService postService;
	
	@Autowired
    private CommentService commentService;

	private static final String UPLOAD_DIR = "C:/Users/son/Documents/peachmind-uploads/";

	// 게시판 메인
	@GetMapping(value = "")
	public String board(Model model, HttpSession session, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "filter", defaultValue = "all") String filter) {

		// 인증되지 않은 경우 모든 게시글을 가져옴
		Pageable pageable = PageRequest.of(page, size);
		Page<PostEntity> postPage = postService.findAllPostsWithPagination(pageable);

		model.addAttribute("posts", postPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", postPage.getTotalPages());
		model.addAttribute("message", "모든 게시글을 조회합니다.");

		// 로그인된 사용자의 인증 정보 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
            String username = authentication.getName(); // 로그인한 사용자 정보
            model.addAttribute("username", username);
        }
		
		String currentUsername = authentication.getName(); // 현재 로그인한 사용자 이름
		model.addAttribute("currentUsername", currentUsername); // 현재 사용자 정보 전달


		return "peachmind/board/main";
	}

	// 글 작성 폼 페이지로 이동
	@GetMapping("/write")
	public String showCreatePostForm(Model model) {
		model.addAttribute("post", new PostEntity());
		return "peachmind/board/write";
	}

	// 글 작성 처리
	@PostMapping("/write")
	public String createPost(@ModelAttribute PostEntity newPost, @RequestParam("file") MultipartFile file, Model model)
			throws IOException {
		System.out.println("글 등록 진행 : ");

		// 로그인한 사용자의 username 가져오기
		String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

		// 게시글 작성
		newPost.setAuthor(username);
		// 현재 시간을 createdDate 필드에 저장
		newPost.setCreatedDate(LocalDateTime.now()); // 현재 시간을 createdDate에 설정

		if (!file.isEmpty()) {
			FileEntity fileEntity = processFile(file, newPost);
			if (fileEntity != null) {
				newPost.getAttachments().add(fileEntity);
			}
		}

		try {
			postService.savePost(newPost);
			System.out.println("게시글 저장");
		} catch (Exception e) {
			model.addAttribute("error", "게시글 작성에 실패했습니다. 다시 시도해 주세요.");
			return "peachmind/board/write";
		}

		return "redirect:/peachmind/board"; // 게시판 페이지로 리디렉션
	}

	// 게시글 상세
	@GetMapping("/post/{id}")
	public String getPost(@PathVariable("id") Long id, Model model) {

		PostEntity post = postService.getPostById(id);

		if (post == null) {
			return "redirect:/peachmind/board"; // 게시글이 없을 경우 게시판 페이지로 리디렉션
		}
		 // 댓글이 null일 경우 빈 리스트로 초기화
	    if (post.getComments() == null) {
	        post.setComments(new ArrayList<>());
	    }
	    // 첨부파일이 null일 경우 빈 리스트로 초기화
	    if (post.getAttachments() == null) {
	        post.setAttachments(new ArrayList<>());
	    }
	    
		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();
		
		String currentUsername = authentication.getName(); // 현재 로그인한 사용자 이름
		
	    
		model.addAttribute("currentUsername", currentUsername); // 현재 사용자 정보 전달

		model.addAttribute("post", post);

		// 댓글 작성 폼에서 사용할 빈 객체 추가
	    CommentEntity comment = new CommentEntity();
	    model.addAttribute("comment", comment);

		return "peachmind/board/detail"; // 상세 페이지 템플릿으로 이동
	}

	// 게시글 삭제
	@GetMapping("/delete/{id}")
	public String deletePost(@PathVariable("id") Long id, Model model) {
	    try {
	        // 게시글에 첨부된 파일들을 먼저 삭제
	        PostEntity post = postService.getPostById(id);
	        if (post != null) {
	            List<FileEntity> attachments = post.getAttachments();
	            for (FileEntity file : attachments) {
	                // 파일 삭제 처리 (파일 시스템에서 삭제)
	                File fileToDelete = new File(file.getFilePath());
	                if (fileToDelete.exists()) {
	                    try {
	                        boolean isDeleted = fileToDelete.delete();  // 파일 삭제 시 결과 확인
	                        if (!isDeleted) {
	                            throw new IOException("파일 삭제 실패: " + fileToDelete.getAbsolutePath());
	                        }
	                    } catch (IOException e) {
	                        // 파일 삭제 실패 예외 처리
	                        System.out.println("파일 삭제 오류: " + e.getMessage());
	                        model.addAttribute("error", "파일 삭제 중 오류가 발생했습니다.");
	                        return "redirect:/peachmind/board"; // 오류 발생 시 게시판 페이지로 리디렉션
	                    }
	                }
	            }
	        }

	        postService.deletePostById(id); // 게시글 삭제
	    } catch (Exception e) {
	        model.addAttribute("error", "게시글 삭제에 실패했습니다.");
	        return "redirect:/peachmind/board"; // 삭제 실패 시 게시판 페이지로 리디렉션
	    }
	    return "redirect:/peachmind/board"; // 삭제 후 게시판 페이지로 리디렉션
	}

	@ModelAttribute("userName")
	public String getUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			return authentication.getName(); // 인증된 사용자의 이름 반환
		}
		return null; // 인증되지 않은 경우 null 반환
	}

	// 게시글 수정 폼으로 이동
	@GetMapping("/edit/{id}")
	public String showEditPostForm(@PathVariable("id") Long id, Model model) {
		PostEntity post = postService.getPostById(id);
		if (post == null) {
			return "redirect:/peachmind/board"; // 게시글이 없으면 게시판 페이지로 리디렉션
		}

		model.addAttribute("post", post); // 수정할 게시글 정보 전달
		return "peachmind/board/edit"; // 수정 페이지 템플릿으로 이동
	}

	// 게시글 수정 진행
	@PostMapping("/edit/{id}")
	public String updatePost(@PathVariable("id") Long id, @ModelAttribute PostEntity updatedPost,
			@RequestParam("file") MultipartFile file, Model model) throws IOException {
		PostEntity existingPost = postService.getPostById(id);
		if (existingPost == null) {
			return "redirect:/peachmind/board"; // 게시글이 없으면 게시판 페이지로 리디렉션
		}

		// 제목과 내용 업데이트
		existingPost.setTitle(updatedPost.getTitle());
		existingPost.setContent(updatedPost.getContent());

		if (!file.isEmpty()) {
			FileEntity fileEntity = processFile(file, existingPost);
			if (fileEntity != null) {
				existingPost.getAttachments().add(fileEntity);
			}
		}

		try {
			postService.savePost(existingPost);
		} catch (Exception e) {
			model.addAttribute("error", "게시글 수정에 실패했습니다. 다시 시도해 주세요.");
			return "peachmind/board/edit";
		}

		return "redirect:/peachmind/board"; // 수정 후 게시판 페이지로 리디렉션
	}

	// 파일 삭제진행
	@DeleteMapping("/files/delete/{fileId}")
	public ResponseEntity<?> deleteFile(@PathVariable("fileId") Long fileId) {
	    try {
	        postService.deleteFileById(fileId); // 파일 삭제 처리
	        return ResponseEntity.ok().body(Map.of("status", "success"));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of("status", "fail", "message", "파일 삭제 실패"));
	    }
	}
	
	
	// 댓글 작성 진행
	@PostMapping("/post/{postId}/comment")
	public String postComment(@PathVariable("postId") Long postId,
	                          @RequestParam("content") String content,
	                          HttpSession session) {
		
		// 로그인된 사용자의 인증 정보 가져오기
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String author = authentication.getName();  // 인증된 사용자의 username 가져오기
		
	    System.out.println(author); 
		
	    
	    if (author == null) {
	        // 로그인하지 않은 경우 처리 (예: 로그인 페이지로 리다이렉트)
	        return "redirect:/login";
	    }
		
	    // 게시글 조회
	    PostEntity post = postService.getPostById(postId);

	    if (post == null) {
	        // 게시글이 없는 경우 게시판 페이지로 리다이렉트
	        return "redirect:/peachmind/board";
	    }

	    // 새로운 댓글 생성
	    CommentEntity comment = new CommentEntity();
	    comment.setPost(post);
	    comment.setContent(content);
	    comment.setAuthor(author);

	    // 댓글 저장
	    commentService.saveComment(comment);

	    // 댓글 작성 후 게시글 상세 페이지로 리다이렉트
	    return "redirect:/peachmind/board/post/" + postId;  // 수정된 부분
	}

	
	// 댓글 삭제
	@GetMapping("/post/{postId}/comment/delete/{commentId}")
	public String deleteComment(@PathVariable("postId") Long postId,
	                             @PathVariable("commentId") Long commentId,
	                             Model model) {
	    // 댓글을 가져옵니다.
	    CommentEntity comment = commentService.getCommentById(commentId);

	    // 댓글이 존재하지 않으면 게시글 상세 페이지로 리디렉션
	    if (comment == null) {
	        return "redirect:/peachmind/board/post/" + postId;
	    }

	    // 로그인한 사용자 정보 가져오기
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String currentUsername = authentication.getName();

	    // 댓글 작성자와 로그인한 사용자가 다르면 삭제할 수 없습니다.
	    if (!comment.getAuthor().equals(currentUsername)) {
	        model.addAttribute("error", "권한이 없습니다.");
	        return "redirect:/peachmind/board/post/" + postId;
	    }

	    // 댓글 삭제
	    commentService.deleteComment(commentId);

	    return "redirect:/peachmind/board/post/" + postId;  // 삭제 후 게시글 상세 페이지로 리디렉션
	}
	
	@PostMapping("/post/{postId}/comment/reply/{commentId}")
	public String postReply(@PathVariable("postId") Long postId,
	                        @PathVariable("commentId") Long commentId,
	                        @RequestParam("content") String content,
	                        Model model) {
		
	    // 로그인된 사용자의 인증 정보 가져오기
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String author = authentication.getName();  // 인증된 사용자의 username 가져오기

	    if (author == null) {
	        // 로그인하지 않은 경우 처리 (예: 로그인 페이지로 리다이렉트)
	        return "redirect:/login";
	    }

	    // 게시글 조회
	    PostEntity post = postService.getPostById(postId);
	    if (post == null) {
	        return "redirect:/peachmind/board";
	    }

	    // 댓글 조회
	    CommentEntity parentComment = commentService.getCommentById(commentId);
	    if (parentComment == null) {
	        return "redirect:/peachmind/board";
	    }

	    // 답글 생성
	    CommentEntity reply = new CommentEntity();
	    reply.setPost(post);
	    reply.setContent(content);
	    reply.setAuthor(author);
	    reply.setParentComment(parentComment); // 부모 댓글 설정

	    // 답글 저장
	    commentService.saveComment(reply);

	    // 답글 작성 후 게시글 상세 페이지로 리다이렉트
	    return "redirect:/peachmind/board/post/" + postId;  // 이 부분이 올바르게 작동해야 합니다.
	}

	
	/**
	 * 인증 상태에 따라 게시글을 조회하는 유틸리티 메서드
	 */
	private Page<PostEntity> getPostsForUser(Authentication authentication, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			return postService.findPostsWithPagination(authentication.getName(), pageable);
		}
		return postService.findAllPostsWithPagination(pageable);
	}

	// 공통 파일 처리 메서드
	private FileEntity processFile(MultipartFile file, PostEntity post) throws IOException {
		if (file.isEmpty()) {
			return null;
		}

		File uploadDirPath = new File(UPLOAD_DIR);
		if (!uploadDirPath.exists()) {
			uploadDirPath.mkdirs();
		}

		String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		String filePath = UPLOAD_DIR + uniqueFileName;
		file.transferTo(new File(filePath));
		
		FileEntity fileEntity = new FileEntity();
		// FileEntity에 고유 파일명 저장
		fileEntity.setFileName(uniqueFileName);
		fileEntity.setFilePath(filePath);
		fileEntity.setFileType(file.getContentType());
		fileEntity.setFileSize(file.getSize());
		fileEntity.setPost(post);

		return fileEntity;
	}

}
