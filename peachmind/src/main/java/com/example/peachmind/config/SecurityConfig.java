package com.example.peachmind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// 보안관련 설정
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 암호화 인코딩
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // 보안 및 로그인 관련 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/peachmind/login", "/peachmind/join",  "/h2-console/**").permitAll() // 로그인, 회원가입 페이지 접근 허용
                //.requestMatchers("/h2-console").hasRole("ADMIN") // 관리자만 접근 가능
                //.requestMatchers("/peachmind/board").hasRole("USER") // 일반 사용자 접근 가능
                .anyRequest().authenticated() // 그 외 요청은 인증 필요
            )
            .formLogin(form -> form
                .loginPage("/peachmind/login")  // 로그인 페이지 경로 설정
                .defaultSuccessUrl("/peachmind/board", true) // 기본 로그인 성공 시 이동 경로 (일반 사용자)
                .failureUrl("/peachmind/login?error=true") // 로그인 실패 시 이동 경로
                .failureHandler((request, response, exception) -> {
                	 // 로그인 실패 시 예외 처리
                    String errorMessage = "로그인에 실패했습니다. 아이디와 비밀번호를 확인해 주세요.";
                    if (exception instanceof BadCredentialsException) {
                        request.getSession().setAttribute("error", errorMessage);
                    }
                    response.sendRedirect("/peachmind/login?error=true");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/peachmind/logout") // 로그아웃 요청 URL
                .logoutSuccessUrl("/peachmind/login?logout=true") // 로그아웃 성공 시 리디렉션 URL
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()) 
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())) // H2 Console을 위한 설정
        	.sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 세션이 필요한 경우에만 생성
                    .maximumSessions(1) // 동시에 여러 세션을 만들지 않도록 설정
                    .expiredUrl("/peachmind/login?expired=true") // 세션 만료 후 리디렉션 URL
                    .and()
                    .invalidSessionUrl("/peachmind/login?invalid=true") // 유효하지 않은 세션 처리
                
            );
        
        return http.build();
    }

    // AuthenticationManager 설정
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);

        // in-memory 인증 설정
        authenticationManagerBuilder
            .userDetailsService(userDetailsService()) // UserDetailsService 추가
            .passwordEncoder(passwordEncoder());

        return authenticationManagerBuilder.build();
    }

    // InMemoryUserDetailsManager 사용하여 사용자 관리
    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        // 기본 admin, user 계정 설정
        manager.createUser(User.withUsername("admin").password(passwordEncoder().encode("1111")).roles("ADMIN").build());
        manager.createUser(User.withUsername("user").password(passwordEncoder().encode("1234")).roles("USER").build());
        manager.createUser(User.withUsername("son").password(passwordEncoder().encode("1234")).roles("USER").build());

        return manager;
    }
    
    // 정적 리소스에 대한 보안 필터 무시 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers("/css/**", "/js/**", "/images/**"); // 정적 리소스 경로 설정
    }
    
}
