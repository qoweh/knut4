package com.knut4.backend.domain.auth.controller;

import com.knut4.backend.domain.auth.dto.UserResponse;
import com.knut4.backend.domain.auth.service.UserService;
import com.knut4.backend.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private")
public class UserMeController {

    private final UserService userService;

    public UserMeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return UserResponse.from(user);
    }
}
