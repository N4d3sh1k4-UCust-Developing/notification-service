package com.n4d3sh1k4.notification_service.service;

import ch.qos.logback.classic.pattern.EnsureExceptionHandling;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${email.support}")
    String supportEmail;

    @Value("${url.backend}")
    String backendUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendRegistrationEmail(String to, String username, String token, String accountActivationTokenTtl) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("activationUrl", backendUrl + "/api/v0/auth/confirm-email?token=" + token);
        context.setVariable("expiryMinutes", accountActivationTokenTtl);
        context.setVariable("supportEmail", supportEmail);

        String htmlContent = templateEngine.process("account-activate-email", context);

        sendHtmlEmail(to, "Подтверждение регистрации", htmlContent);
        log.info("Email sent to {}", to);
    }

    public void sendResetPasswordEmail(String to, String token, String passwordResetTokenTtl) {
        Context context = new Context();
        context.setVariable("activationUrl", backendUrl + "/reset-password?token=" + token);
        context.setVariable("expiryMinutes", passwordResetTokenTtl);
        context.setVariable("supportEmail", supportEmail);

        String htmlContent = templateEngine.process("password-reset-email", context);

        sendHtmlEmail(to, "Сброс пароля", htmlContent);
        log.info("Reset password email sent to {}", to);
    }

    public void sendAccountLockedEmail(String to, Instant time, String accountLockedCooldown) {
        Context context = new Context();
        context.setVariable("lockDate", " в "+time);
        context.setVariable("lockedTime", accountLockedCooldown);
        context.setVariable("supportEmail", supportEmail);

        String htmlContent = templateEngine.process("account-locked-email", context);

        sendHtmlEmail(to, "Сброс пароля", htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException _) {
        }
    }
}