package com.example.xuandangwebbanhang.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String configuredFromEmail;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    public void sendRewardOtp(String toEmail, String otpCode) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Email nguoi nhan khong hop le");
        }

        String fromEmail = configuredFromEmail != null && !configuredFromEmail.trim().isEmpty()
                ? configuredFromEmail.trim()
                : (smtpUsername == null ? "" : smtpUsername.trim());

        if (fromEmail.isEmpty()) {
            throw new IllegalStateException("Chua cau hinh email gui. Hay set spring.mail.username hoac app.mail.from");
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromEmail);
        msg.setTo(toEmail.trim());
        msg.setSubject("[XuanDang Shop] Ma xac nhan doi diem");
        msg.setText("Xin chao,\n\n"
                + "Ma OTP xac nhan doi diem cua ban la: " + otpCode + "\n"
                + "Ma co hieu luc trong 5 phut.\n\n"
                + "Neu ban khong thuc hien yeu cau nay, vui long bo qua email nay.\n\n"
                + "XuanDang Shop");
        mailSender.send(msg);
    }
}
