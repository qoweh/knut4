package com.knut4.backend.domain.auth;

import com.knut4.backend.domain.auth.dto.SignUpRequest;
import com.knut4.backend.domain.auth.service.UserService;
import com.knut4.backend.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(UserServiceTest.Config.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원 가입 성공")
    void register_success() {
        var req = new SignUpRequest("alice","password123","1990-01-02");
        var user = userService.register(req);
        assertThat(user.getId()).isNotNull();
        assertThat(userRepository.existsByUsername("alice")).isTrue();
    }

    @Test
    @DisplayName("중복 아이디 예외")
    void duplicate_username() {
        var req = new SignUpRequest("bob","password123","1995-05-10");
        userService.register(req);
        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class Config {
        @org.springframework.context.annotation.Bean
        public UserService userService(UserRepository repo, PasswordEncoder encoder) {
            return new UserService(repo, encoder);
        }
        @org.springframework.context.annotation.Bean
        public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }
}
