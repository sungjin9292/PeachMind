package com.example.peachmind.config;

import com.example.peachmind.repository.MemberRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

	private final MemberRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DataLoader(MemberRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		// 비밀번호 인코딩 예시
		//String encodedPassword = passwordEncoder.encode("1234");
		//System.out.println("Encoded password: " + encodedPassword);
//        User user1 = new User();
//        user1.setName("admin");
//        user1.setEmail("admin.doe@example.com");
//        user1.setPassword("1234");
//      
//        userRepository.save(user1);
//
//        System.out.println("Sample users saved!");
	}
}
