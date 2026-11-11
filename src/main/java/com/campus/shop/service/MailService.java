package com.campus.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务（JavaMailSender）
 * <p>配置里如果还是占位邮箱，发送会跳过但不抛异常，
 * 只记录日志 —— 避免演示时因为没配邮箱把整条链路搞挂。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public boolean send(String to, String subject, String content) {
        if (to == null || to.isBlank()) {
            log.warn("[邮件] 收件人为空，跳过发送 subject={}", subject);
            return false;
        }
        if (from == null || from.isBlank() || from.contains("placeholder")) {
            log.warn("[邮件] 发件邮箱未配置（配置里是占位符），跳过发送 to={} subject={}", to, subject);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("[邮件] 发送成功 to={} subject={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("[邮件] 发送失败 to={} subject={} err={}", to, subject, e.getMessage());
            return false;
        }
    }
}