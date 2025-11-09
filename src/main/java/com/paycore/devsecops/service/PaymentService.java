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
        if (value == null) return null;
        return value.replaceAll("[\r\n]", "_");
    }

    public String processPayment(PaymentRequest request, String clientId) {
        String safeClientId = sanitizeForLog(clientId);

        log.info("Processing payment for companyId={}, amount={}, currency={}",
                safeClientId, request.getAmount(), request.getCurrency());

        String operationId = "OP-" + UUID.randomUUID();

        String sql = "INSERT INTO payments " +
                "(operation_id, company_id, payer_name, payer_email, cbu, amount, currency, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, operationId);
            ps.setString(2, request.getCompanyId());
            ps.setString(3, request.getPayerName());
            ps.setString(4, request.getPayerEmail());
            ps.setString(5, request.getCbu());
            ps.setBigDecimal(6, request.getAmount());
            ps.setString(7, request.getCurrency());
            ps.setString(8, request.getDescription());

            int rows = ps.executeUpdate();
            log.info("Inserted {} row(s) for operationId={}", rows, operationId);

        } catch (SQLException e) {
            log.error("Error executing SQL for operationId={}", operationId, e);
        }

        callErpApi(operationId, request.getAmount(), safeClientId);

        return operationId;
    }

    private void callErpApi(String operationId, BigDecimal amount, String clientId) {
        String url = erpBaseUrl + "/payments/sync";

        log.info("Calling ERP endpoint {} for operationId={}, clientId={}, amount={}",
                url, clientId, operationId, amount);

    }
}
