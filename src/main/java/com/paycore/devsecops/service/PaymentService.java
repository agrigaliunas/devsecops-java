package com.paycore.devsecops.service;


import com.paycore.devsecops.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * ESTA CLASE ESTÁ LLENA DE MALAS PRÁCTICAS A PROPÓSITO.
 * Es ideal para que herramientas SAST/DAST la marquen.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // MALA PRÁCTICA: secretos hardcodeados en código
    private static final String DB_URL = "jdbc:mysql://localhost:3306/paycore";
    private static final String DB_USER = "paycore_user";
    private static final String DB_PASSWORD = "SuperInsecurePassword123";

    // MALA PRÁCTICA: token de API hardcodeado
    private static final String ERP_API_TOKEN = "erp-secret-token-very-insecure";

    /**
     * Método vulnerabile:
     * - SQL injection (string concatenation)
     * - Logging excesivo de datos sensibles
     * - Uso de java.util.Random para IDs relacionados a operaciones
     * - Sin uso de HTTPS ni validación de certificados (simulado)
     */
    public String processPaymentInsecure(PaymentRequest request, String clientId) {
        log.debug("Starting insecure processing for companyId={}, email={}, cbu={}, amount={}",
                clientId, request.getPayerEmail(), request.getCbu(), request.getAmount());

        String operationId = "OP-" + new Random().nextInt(999999);

        String sql = "INSERT INTO payments (operation_id, company_id, payer_name, payer_email, cbu, amount, currency, description) " +
                "VALUES ('" + operationId + "', '" + request.getCompanyId() + "', '" + request.getPayerName() + "', '" +
                request.getPayerEmail() + "', '" + request.getCbu() + "', " + request.getAmount() + ", '" +
                request.getCurrency() + "', '" + request.getDescription() + "')";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            int rows = statement.executeUpdate(sql);
            log.info("Inserted {} row(s) for operationId={}", rows, operationId);

        } catch (SQLException e) {
            log.error("Error executing SQL for operationId=" + operationId + ": " + e.getMessage(), e);
        }

        callInsecureErpApi(operationId, request.getAmount(), clientId);

        return operationId;
    }

    /**
     * Simula una llamada HTTP insegura a un ERP corporativo.
     */
    private void callInsecureErpApi(String operationId, BigDecimal amount, String clientId) {
        String url = "http://insecure-erp.fintechinnovate.local/payments/sync?operationId="
                + operationId + "&amount=" + amount + "&clientId=" + clientId;

        // MALA PRÁCTICA: incluir token de API en query string
        url += "&token=" + ERP_API_TOKEN;

        log.warn("Calling insecure ERP endpoint: {}", url);
    }
}