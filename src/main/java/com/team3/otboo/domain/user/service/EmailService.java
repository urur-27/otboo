package com.team3.otboo.domain.user.service;

public interface EmailService {
    void sendTempPasswordResetEmail(String resetEmail, String tempPassword);
}
