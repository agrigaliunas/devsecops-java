package com.paycore.devsecops.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String health() {
        return "<html><body>" +
                "<h1>PayCore</h1>" +
                "<a href=\"/api/payments/preview?description=test\" rel=\"noopener noreferrer\">Preview</a>" +
                "<a href=\"/api/payments/redirect?url=/home\" rel=\"noopener noreferrer\">Redirect</a>" +
                "<a href=\"/api/payments/debug?error=ok\" rel=\"noopener noreferrer\">Debug</a>" +
                "</body></html>";
    }
}
