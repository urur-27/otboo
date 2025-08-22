package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// 서버 시작 시 어드민 계정 초기화

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private final AuthService authService;

    public void run(ApplicationArguments args) {
        authService.initAdmin();
    }
}
