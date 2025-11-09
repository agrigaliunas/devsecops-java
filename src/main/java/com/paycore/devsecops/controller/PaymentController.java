package com.paycore.devsecops.controller;

import com.paycore.devsecops.dto.PaymentRequest;
import com.paycore.devsecops.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private static final Pattern SAFE_RELATIVE_URL_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_-]*$");

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "null";
        }
        return value.replaceAll("[\\r\\n\\t\\x00-\\x1F\\x7F]", "_");
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest request,
                                                 @RequestHeader(value = "X-Client-Id", required = false) String clientId) {

        String safeClientId = sanitizeForLog(clientId);
        String safeCurrency = sanitizeForLog(request.getCurrency());

        log.info("Processing payment for clientId={}, amount={}, currency={}",
                safeClientId, request.getAmount(), safeCurrency);

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        String operationId = paymentService.processPayment(request, clientId);

        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .body("Payment processed with operationId=" + operationId);
    }

    @GetMapping("/preview")
    public ResponseEntity<String> previewPayment(@RequestParam(name = "description", required = false) String description) {
        if (description == null) {
            description = "No description provided";
        }

        String safeDescription = HtmlUtils.htmlEscape(description);

        String html = "<!DOCTYPE html><html><head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta http-equiv=\"X-Content-Type-Options\" content=\"nosniff\">" +
                "</head><body>" +
                "<h1>Payment preview</h1>" +
                "<p>Description: " + safeDescription + "</p>" +
                "</body></html>";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html; charset=UTF-8");
        headers.add("X-Content-Type-Options", "nosniff");

        return ResponseEntity.ok()
                .headers(headers)
                .body(html);
    }

    @GetMapping("/redirect")
    public ResponseEntity<Void> redirectTo(@RequestParam("url") String url) throws URISyntaxException {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!url.startsWith("/") || !SAFE_RELATIVE_URL_PATTERN.matcher(url).matches()) {
            String safeUrl = sanitizeForLog(url);
            log.warn("Rejected unsafe redirect URL: {}", safeUrl);
            return ResponseEntity.badRequest().build();
        }

        URI location = new URI(url);

        return ResponseEntity.status(302)
                .header("X-Content-Type-Options", "nosniff")
                .location(location)
                .build();
    }

    @GetMapping("/debug")
    public ResponseEntity<String> debug(@RequestParam(value = "error", required = false) String error) {
        try {
            String safeError = sanitizeForLog(error);

            if ("fail".equals(error)) {
                log.warn("Forced debug error triggered with param: {}", safeError);
                return ResponseEntity.status(500)
                        .header("X-Content-Type-Options", "nosniff")
                        .body("Forced debug error");
            }

            return ResponseEntity.ok()
                    .header("X-Content-Type-Options", "nosniff")
                    .body("Debug OK");
        } catch (Exception e) {
            log.error("Debug endpoint error", e);
            return ResponseEntity.status(500)
                    .header("X-Content-Type-Options", "nosniff")
                    .body("Internal server error");
        }
    }
}