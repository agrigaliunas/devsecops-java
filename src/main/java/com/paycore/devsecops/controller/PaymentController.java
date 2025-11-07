package com.paycore.devsecops.controller;


import com.paycore.devsecops.dto.PaymentRequest;
import com.paycore.devsecops.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Ejemplo de endpoint vulnerable:
     * - Loggea información sensible completa
     * - No valida bien el input
     */
    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest request,
                                                 @RequestHeader(value = "X-Client-Id", required = false) String clientId) {

        log.info("Processing payment for companyId={}, request={}", clientId, request);

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        String operationId = paymentService.processPaymentInsecure(request, clientId);

        return ResponseEntity.ok("Payment processed with operationId=" + operationId);
    }
}