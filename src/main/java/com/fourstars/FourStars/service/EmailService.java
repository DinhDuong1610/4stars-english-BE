package com.fourstars.FourStars.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your Password Reset OTP Code");
            message.setText("Hello,\n\nYour One-Time Password (OTP) for resetting your password is: "
                    + otp + "\n\nThis code is valid for 5 minutes.");

            mailSender.send(message);
            logger.info("Successfully sent OTP email to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}", toEmail, e);
        }
    }
}
