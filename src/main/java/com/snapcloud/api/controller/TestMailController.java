package com.snapcloud.api.controller;

import com.snapcloud.api.util.SendMailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-mail")
@RequiredArgsConstructor
public class TestMailController {

    private final SendMailUtil sendMailUtil;

    @GetMapping("/send")
    public String testSendGrid() {
        boolean sent = sendMailUtil.sendEmail(
            "nethushiperera03@gmail.com",
            "SendGrid Test Email",
            "Hello! This is a SendGrid test from SnapCloud."
        );
        return sent ? "Email sent successfully" : "Email failed";
    }
}
