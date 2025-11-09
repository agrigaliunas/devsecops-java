package com.paycore.devsecops.service;

import com.paycore.devsecops.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final DataSource dataSource;
    private final String erpBaseUrl;
    private final String erpApiToken;

    public PaymentService(DataSource dataSource,
                          @Value("${paycore.erp.base-url}") String erpBaseUrl,
                          @Value("${paycore.erp.api-token:ci-fake-token}") String erpApiToken) {
        this.dataSource = dataSource;
        this.erpBaseUrl = erpBaseUrl;
        this.erpApiToken = erpApiToken;
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "null";
        }
        return value.replaceAll("[\\r\\n\\t\\x00-\\x1F\\x7F]", "_");
    }

    public String processPayment(PaymentRequest request, String clientId) {
        // Crear copia defensiva de los datos del request
        String companyId = request.getCompanyId();
        String payerName = request.getPayerName();
        String payerEmail = request.getPayerEmail();
        String cbu = request.getCbu();
        BigDecimal amount = request.getAmount();
        String currency = request.getCurrency();
        String description = request.getDescription();

        String safeClientId = sanitizeForLog(clientId);
        String safeCompanyId = sanitizeForLog(companyId);
        String safeCurrency = sanitizeForLog(currency);

        log.info("Processing payment for companyId={}, amount={}, currency={}",
                safeCompanyId, amount, safeCurrency);

        String operationId = "OP-" + UUID.randomUUID();

        String sql = "INSERT INTO payments " +
                "(operation_id, company_id, payer_name, payer_email, cbu, amount, currency, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, operationId);
            ps.setString(2, companyId);
            ps.setString(3, payerName);
            ps.setString(4, payerEmail);
            ps.setString(5, cbu);
            ps.setBigDecimal(6, amount);
            ps.setString(7, currency);
            ps.setString(8, description);

            int rows = ps.executeUpdate();
            String safeOperationId = sanitizeForLog(operationId);
            log.info("Inserted {} row(s) for operationId={}", rows, safeOperationId);

        } catch (SQLException e) {
            String safeOperationId = sanitizeForLog(operationId);
            log.error("Error executing SQL for operationId={}", safeOperationId, e);
        }

        callErpApi(operationId, amount, safeClientId);

        return operationId;
    }

    private void callErpApi(String operationId, BigDecimal amount, String clientId) {
        String safeUrl = sanitizeForLog(erpBaseUrl + "/payments/sync");
        String safeOperationId = sanitizeForLog(operationId);
        String safeClientId = sanitizeForLog(clientId);

        log.info("Calling ERP endpoint {} for operationId={}, clientId={}, amount={}",
                safeUrl, safeOperationId, safeClientId, amount);
    }
}