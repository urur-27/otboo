package com.team3.otboo.domain.user.controller;

import com.team3.otboo.domain.user.dto.UserCreateRequest;
import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {


    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> signIn(@RequestParam String email, @RequestParam String password) {
        log.info("로그인 시작: {}", email);

        log.info("로그인 종료: {}", email);
        return null;
    }

}
