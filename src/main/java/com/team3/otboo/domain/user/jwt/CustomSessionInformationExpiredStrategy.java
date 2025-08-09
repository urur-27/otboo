package com.team3.otboo.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.global.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

    private final ObjectMapper objectMapper;

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
            throws IOException, ServletException
    {
        int status = HttpServletResponse.SC_UNAUTHORIZED;
        HttpServletResponse response = event.getResponse();
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String requestURI = event.getRequest().getRequestURI();
        String message = "세션이 만료되었거나 다른 기기에서 로그인하여 로그아웃 처리되었습니다.";
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.SESSION_ERROR, requestURI, message);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
