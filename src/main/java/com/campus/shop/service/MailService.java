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

    /** SMTP 授权码。占位符表示还没填，此时直接跳过发送，避免 MQ 消费线程反复重试拧巴日志 */
    @Value("${spring.mail.password:}")
    private String authCode;

    /** 是否已配置可用的邮箱（发件账号 + 授权码都不为空且不是占位符） */
    public boolean isConfigured() {
        return from != null && !from.isBlank() && !from.contains("placeholder")
                && authCode != null && !authCode.isBlank() && !authCode.contains("placeholder");
    }

    public boolean send(String to, String subject, String content) {
        if (to == null || to.isBlank()) {
            log.warn("[邮件] 收件人为空，跳过发送 subject={}", subject);
            return false;
        }
        if (from == null || from.isBlank() || from.contains("placeholder")) {
            log.warn("[邮件] 发件邮箱未配置（配置里是占位符），跳过发送 to={} subject={}", to, subject);
            return false;
        }
        if (authCode == null || authCode.isBlank() || authCode.contains("placeholder")) {
            log.warn("[邮件] 发件邮箱 {} 已配置，但 SMTP 授权码未填写，跳过发送 to={} subject={}",
                    from, to, subject);
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