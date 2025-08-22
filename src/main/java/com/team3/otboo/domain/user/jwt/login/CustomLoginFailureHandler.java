package com.team3.otboo.domain.user.jwt.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorCode errorCode;
        String errorMessage;

        if(exception instanceof BadCredentialsException){
            errorCode = ErrorCode.INVALID_INPUT_VALUE;
            errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        } else if(exception instanceof DisabledException){
            errorCode = ErrorCode.ACCESS_DENIED;
            errorMessage = "비활성화된 계정입니다.";
        } else {
            errorCode = ErrorCode.SIGN_IN_ERROR;
            errorMessage = exception.getMessage();
        }
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, request.getRequestURI(), errorMessage);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
