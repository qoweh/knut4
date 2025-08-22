package com.knut4.backend1.auth.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 사용자 정보를 로드하는 서비스
 * 현재는 하드코딩된 사용자로 테스트 목적으로 구현
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 임시 하드코딩된 사용자 (실제로는 데이터베이스에서 조회)
        if ("testuser".equals(username)) {
            return User.builder()
                    .username("testuser")
                    .password(passwordEncoder.encode("testpass"))
                    .authorities(Collections.emptyList())
                    .build();
        }
        
        throw new UsernameNotFoundException("User not found: " + username);
    }
    
    /**
     * 비밀번호 인코더 반환 (인증 시 사용)
     */
    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}