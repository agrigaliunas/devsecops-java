package com.paycore.devsecops.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> health() {
        String html = "<!DOCTYPE html><html><head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta http-equiv=\"X-Content-Type-Options\" content=\"nosniff\">" +
                "</head><body>" +
                "<h1>PayCore</h1>" +
                "<a href=\"/api/payments/preview?description=test\" rel=\"noopener noreferrer\">Preview</a><br>" +
                "<a href=\"/api/payments/redirect?url=/home\" rel=\"noopener noreferrer\">Redirect</a><br>" +
                "<a href=\"/api/payments/debug?error=ok\" rel=\"noopener noreferrer\">Debug</a>" +
                "</body></html>";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html; charset=UTF-8");
        headers.add("X-Content-Type-Options", "nosniff");

        return ResponseEntity.ok()
                .headers(headers)
                .body(html);
    }
}