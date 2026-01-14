package com.example.testsecurity.util;

import com.example.testsecurity.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để generate sample data cho database.
 * <p>
 * Class này tạo ra các SQL INSERT statements với Account numbers đã được mã hóa AES.
 * <p>
 * <b>Cách sử dụng:</b>
 * <ol>
 *     <li>Chạy ứng dụng Spring Boot</li>
 *     <li>Class này sẽ tự động chạy và generate SQL statements</li>
 *     <li>Copy SQL statements từ console và chạy trong MySQL</li>
 * </ol>
 * <p>
 * <b>Lưu ý:</b>
 * <ul>
 *     <li>Chỉ chạy trong development environment</li>
 *     <li>Account numbers được mã hóa với AES key từ application.yaml</li>
 *     <li>Mỗi lần chạy sẽ generate data mới (encrypted values khác nhau do IV random)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataGenerator {

    private final EncryptionService encryptionService;

    private static final DateTimeFormatter SQL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generate sample transaction data với Account numbers đã được mã hóa
     */
    public void generateSampleData() {
        try {
            List<String> sqlStatements = new ArrayList<>();

            sqlStatements.add("-- =====================================================");
            sqlStatements.add("-- Banking Transaction System - Sample Data (AES Encrypted)");
            sqlStatements.add("-- =====================================================");
            sqlStatements.add("-- Generated at: " + LocalDateTime.now());
            sqlStatements.add("");
            sqlStatements.add("DELETE FROM transaction_history;");
            sqlStatements.add("ALTER TABLE transaction_history AUTO_INCREMENT = 1;");
            sqlStatements.add("");

            // Sample accounts
            String[] accounts = {"1234567890", "9876543210", "1111111111", "2222222222", "3333333333"};

            // Encrypt accounts
            String[] encryptedAccounts = new String[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                encryptedAccounts[i] = com.example.testsecurity.util.AesUtil.encrypt(
                        accounts[i],
                        com.example.testsecurity.util.AesUtil.keyFromString(aesKeyBase64)
                );
                log.info("Encrypted account {}: {}", accounts[i], encryptedAccounts[i].substring(0, Math.min(50, encryptedAccounts[i].length())) + "...");
            }

            // Transaction 1: 1234567890 → 9876543210, 1,000,000 VND
            String txId1 = "TXN20240115001";
            LocalDateTime time1 = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            sqlStatements.add(String.format(
                    "-- Transaction 1: %s → %s, 1,000,000 VND",
                    accounts[0], accounts[1]
            ));
            sqlStatements.add(String.format(
                    "INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES\n" +
                            "('%s', '%s', %s, %s, '%s'),\n" +
                            "('%s', '%s', %s, %s, '%s');",
                    txId1, encryptedAccounts[0], "1000000.00", "0.00", time1.format(SQL_DATE_FORMAT),
                    txId1, encryptedAccounts[1], "0.00", "1000000.00", time1.format(SQL_DATE_FORMAT)
            ));
            sqlStatements.add("");

            // Transaction 2: 9876543210 → 1111111111, 500,000 VND
            String txId2 = "TXN20240115002";
            LocalDateTime time2 = LocalDateTime.of(2024, 1, 15, 11, 15, 0);
            sqlStatements.add(String.format(
                    "-- Transaction 2: %s → %s, 500,000 VND",
                    accounts[1], accounts[2]
            ));
            sqlStatements.add(String.format(
                    "INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES\n" +
                            "('%s', '%s', %s, %s, '%s'),\n" +
                            "('%s', '%s', %s, %s, '%s');",
                    txId2, encryptedAccounts[1], "500000.00", "0.00", time2.format(SQL_DATE_FORMAT),
                    txId2, encryptedAccounts[2], "0.00", "500000.00", time2.format(SQL_DATE_FORMAT)
            ));
            sqlStatements.add("");

            // Transaction 3: 1111111111 → 1234567890, 2,500,000 VND
            String txId3 = "TXN20240115003";
            LocalDateTime time3 = LocalDateTime.of(2024, 1, 15, 14, 20, 0);
            sqlStatements.add(String.format(
                    "-- Transaction 3: %s → %s, 2,500,000 VND",
                    accounts[2], accounts[0]
            ));
            sqlStatements.add(String.format(
                    "INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES\n" +
                            "('%s', '%s', %s, %s, '%s'),\n" +
                            "('%s', '%s', %s, %s, '%s');",
                    txId3, encryptedAccounts[2], "2500000.00", "0.00", time3.format(SQL_DATE_FORMAT),
                    txId3, encryptedAccounts[0], "0.00", "2500000.00", time3.format(SQL_DATE_FORMAT)
            ));
            sqlStatements.add("");

            // Transaction 4: 2222222222 → 3333333333, 750,000 VND
            String txId4 = "TXN20240115004";
            LocalDateTime time4 = LocalDateTime.of(2024, 1, 15, 16, 45, 0);
            sqlStatements.add(String.format(
                    "-- Transaction 4: %s → %s, 750,000 VND",
                    accounts[3], accounts[4]
            ));
            sqlStatements.add(String.format(
                    "INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES\n" +
                            "('%s', '%s', %s, %s, '%s'),\n" +
                            "('%s', '%s', %s, %s, '%s');",
                    txId4, encryptedAccounts[3], "750000.00", "0.00", time4.format(SQL_DATE_FORMAT),
                    txId4, encryptedAccounts[4], "0.00", "750000.00", time4.format(SQL_DATE_FORMAT)
            ));
            sqlStatements.add("");

            // Transaction 5: 3333333333 → 1234567890, 1,500,000 VND
            String txId5 = "TXN20240115005";
            LocalDateTime time5 = LocalDateTime.of(2024, 1, 15, 18, 0, 0);
            sqlStatements.add(String.format(
                    "-- Transaction 5: %s → %s, 1,500,000 VND",
                    accounts[4], accounts[0]
            ));
            sqlStatements.add(String.format(
                    "INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES\n" +
                            "('%s', '%s', %s, %s, '%s'),\n" +
                            "('%s', '%s', %s, %s, '%s');",
                    txId5, encryptedAccounts[4], "1500000.00", "0.00", time5.format(SQL_DATE_FORMAT),
                    txId5, encryptedAccounts[0], "0.00", "1500000.00", time5.format(SQL_DATE_FORMAT)
            ));

            // Print SQL statements
            log.info("\n" + "=".repeat(80));
            log.info("SAMPLE DATA SQL STATEMENTS (Copy và chạy trong MySQL):");
            log.info("=".repeat(80));
            for (String sql : sqlStatements) {
                log.info(sql);
            }
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("Failed to generate sample data", e);
        }
    }
}
