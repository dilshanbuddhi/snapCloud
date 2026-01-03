package com.snapcloud.api.util;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SendMailUtil {

    private static final Logger log = LoggerFactory.getLogger(SendMailUtil.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.type}")
    private String mailType;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.sendgrid-api-key:}")
    private String sendGridApiKey;

    // Backward-compatible / alternate key
    @Value("${sendgrid.api-key:}")
    private String sendGridApiKeyAlt;

    private SendGrid sendGrid;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(fromAddress)) {
            log.warn("app.mail.from is empty; outgoing mail may fail");
        }

        // prefer app.mail.sendgrid-api-key, fallback to sendgrid.api-key
        if (!StringUtils.hasText(sendGridApiKey) && StringUtils.hasText(sendGridApiKeyAlt)) {
            sendGridApiKey = sendGridApiKeyAlt;
        }

        if ("sendgrid".equalsIgnoreCase(mailType)) {
            if (!StringUtils.hasText(sendGridApiKey)) {
                log.error("Mail type is sendgrid but no API key configured (app.mail.sendgrid-api-key / sendgrid.api-key)");
            } else {
                sendGrid = new SendGrid(sendGridApiKey);
            }
        }

        log.info("Mail initialized | type={} | from={}", mailType, fromAddress);
    }

    public boolean sendEmail(String to, String subject, String text) {
        if ("sendgrid".equalsIgnoreCase(mailType)) {
            boolean sent = sendViaSendGrid(to, subject, text);
            if (!sent) {
                log.warn("SendGrid failed → fallback to SMTP");
                return sendViaSmtp(to, subject, text);
            }
            return true;
        }
        return sendViaSmtp(to, subject, text);
    }

    private boolean sendViaSmtp(String to, String subject, String text) {
        if (mailSender == null) {
            log.error("SMTP not configured");
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("SMTP email sent to {}", to);
            System.out.println("SMTP email sent to " + to);
            return true;
        } catch (Exception e) {
            log.error("SMTP failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendViaSendGrid(String to, String subject, String text) {
        if (sendGrid == null) {
            log.error("SendGrid not configured");
            return false;
        }
        try {
            Email from = new Email(fromAddress);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", text);
            Mail mail = new Mail(from, subject, toEmail, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            System.out.println("SendGrid status: " + response.getStatusCode());
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }
}
