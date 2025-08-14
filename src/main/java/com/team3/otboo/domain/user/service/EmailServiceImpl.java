package com.team3.otboo.domain.user.service;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    public void sendTempPasswordResetEmail(String resetEmail, String tempPassword) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        // 수신자 이메일 주소
        mailMessage.setTo(resetEmail);
        // 이메일 제목
        mailMessage.setSubject("임시 비밀번호 발급 - OTBOO");
        // 이메일 본문
        String text = "안녕하세요. '옷장을 부탁해' 입니다.\n"
                + "요청하신 임시 비밀번호는 아래와 같습니다.\n"
                + "3분 이내에 로그인 후 반드시 비밀번호를 변경해주세요.\n"
                + "임시 비밀번호: " + tempPassword + "\n\n"
                + "다음부터는 잊어버리지 마세요. 제발!!!\n\n"
                + "감사합니다.";
        mailMessage.setText(text);

        try {
            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            log.error("메일 발송 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다.");
        }
    }
}
