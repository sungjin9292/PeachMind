package com.example.peachmind.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.peachmind.entity.MemberEntity;
import com.example.peachmind.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/peachmind")
public class MemberController {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private MemberService memberService;

	// 로그인폼
	@GetMapping("/login")
	public String login(@RequestParam(value = "expired", required = false) String expired,
			Authentication authentication, HttpSession session) {
		
		if (expired != null) {
			// 세션 만료 시 처리
			System.out.println("세션이 만료되었습니다.");
		}

		// 이미 인증된 사용자라면 로그인 페이지 대신 리디렉션 처리
	    if (authentication != null && authentication.isAuthenticated()
	            && !(authentication instanceof AnonymousAuthenticationToken)) {
	        String username = authentication.getName(); // 사용자 이름 가져오기
	        session.setAttribute("username", username); // 세션에 사용자 정보 저장
	        
	        String role = authentication.getAuthorities().iterator().next().getAuthority();
	        if (role.equals("ROLE_ADMIN")) {
	            return "redirect:/h2-console"; // 관리자라면 H2 Console로 리디렉션
	        } else {
	            return "redirect:/peachmind/board"; // 일반 사용자는 board 페이지로 리디렉션
	        }
	    }

		return "peachmind/member/login";
	}
	
	// 로그인된 사용자의 이름을 네비게이션 바에서 사용할 수 있도록 모델에 추가
	@ModelAttribute("userName")
	public String getUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			return authentication.getName(); // 인증된 사용자의 이름 반환
		}
		return null; // 인증되지 않은 경우 null 반환
	}	

}
