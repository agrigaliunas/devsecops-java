package com.paycore.devsecops.controller;

import com.paycore.devsecops.dto.PaymentRequest;
import com.paycore.devsecops.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest request,
                                                 @RequestHeader(value = "X-Client-Id", required = false) String clientId) {

        log.info("Processing payment for companyId={}, amount={}, currency={}",
                clientId, request.getAmount(), request.getCurrency());

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        String operationId = paymentService.processPayment(request, clientId);

        return ResponseEntity.ok("Payment processed with operationId=" + operationId);
    }

    @GetMapping("/preview")
    public ResponseEntity<String> previewPayment(@RequestParam(name = "description", required = false) String description) {
        if (description == null) {
            description = "No description provided";
        }

        String safeDescription = HtmlUtils.htmlEscape(description);

        String html = "<html><body>" +
                "<h1>Payment preview</h1>" +
                "<p>Description: " + safeDescription + "</p>" +
                "</body></html>";

        return ResponseEntity.ok(html);
    }

    @GetMapping("/redirect")
    public ResponseEntity<Void> redirectTo(@RequestParam("url") String url) throws URISyntaxException {
        if (url == null || !url.startsWith("/")) {
            return ResponseEntity.badRequest().build();
        }

        URI location = new URI(url);
        return ResponseEntity.status(302)
                .location(location)
                .build();
    }

    @GetMapping("/debug")
    public ResponseEntity<String> debug(@RequestParam(value = "error", required = false) String error) {
        try {
            if ("fail".equals(error)) {
                throw new RuntimeException("Forced debug error for testing");
            }
            return ResponseEntity.ok("Debug OK");
        } catch (Exception e) {
            log.error("Debug endpoint error", e);
            return ResponseEntity.status(500)
                    .body("Internal server error");
        }
    }
}
