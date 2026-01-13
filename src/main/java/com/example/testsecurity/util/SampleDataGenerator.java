package com.example.testsecurity.util;

import com.example.testsecurity.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class để generate sample data SQL với Account numbers đã được mã hóa AES.
 * <p>
 * <b>Cách sử dụng:</b>
 * <ol>
 *     <li>Chạy ứng dụng với argument: <code>--generate-data</code></li>
 *     <li>Copy SQL statements từ console</li>
 *     <li>Chạy SQL trong MySQL Workbench</li>
 * </ol>
 * <p>
 * <b>Lưu ý:</b>
 * <ul>
 *     <li>Account numbers được mã hóa với AES key từ application.yaml</li>
 *     <li>Mỗi lần chạy sẽ generate encrypted values khác nhau (do IV random)</li>
 *     <li>Chỉ dùng cho development/testing</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SampleDataGenerator implements CommandLineRunner {

    private final EncryptionService encryptionService;

    private static final DateTimeFormatter SQL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generate SQL INSERT statements khi ứng dụng start với flag --generate-data
     */
    @Override
    public void run(String... args) {
        // Chỉ generate data nếu có flag --generate-data
        if (args.length > 0 && args[0].equals("--generate-data")) {
            log.info("=".repeat(80));
            log.info("Generating sample data SQL statements with AES encrypted accounts...");
            log.info("=".repeat(80));
            generateSampleData();
            log.info("=".repeat(80));
            log.info("Copy SQL statements above and run in MySQL Workbench");
            log.info("=".repeat(80));
        }
    }

    /**
     * Generate sample transaction data với Account numbers đã được mã hóa AES
     */
    public void generateSampleData() {
        try {
            // Sample accounts (plaintext)
            String[] accounts = {"1234567890", "9876543210", "1111111111", "2222222222", "3333333333"};

            // Encrypt accounts
            log.info("Encrypting account numbers...");
            String[] encryptedAccounts = new String[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                encryptedAccounts[i] = encryptionService.encryptAccountForDatabase(accounts[i]);
                log.info("  Account {} encrypted: {}...", accounts[i], encryptedAccounts[i].substring(0, Math.min(50, encryptedAccounts[i].length())));
            }

            log.info("\n" + "=".repeat(80));
            log.info("SQL STATEMENTS (Copy và chạy trong MySQL):");
            log.info("=".repeat(80));
            log.info("");

            // SQL Header
            log.info("-- =====================================================");
            log.info("-- Banking Transaction System - Sample Data (AES Encrypted)");
            log.info("-- Generated at: {}", LocalDateTime.now());
            log.info("-- =====================================================");
            log.info("");
            log.info("DELETE FROM transaction_history;");
            log.info("ALTER TABLE transaction_history AUTO_INCREMENT = 1;");
            log.info("");

            // Transaction 1: 1234567890 → 9876543210, 1,000,000 VND
            String txId1 = "TXN20240115001";
            LocalDateTime time1 = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            log.info("-- Transaction 1: {} → {}, 1,000,000 VND", accounts[0], accounts[1]);
            log.info("INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES");
            log.info("('{}', '{}', 1000000.00, 0.00, '{}'),", txId1, encryptedAccounts[0], time1.format(SQL_DATE_FORMAT));
            log.info("('{}', '{}', 0.00, 1000000.00, '{}');", txId1, encryptedAccounts[1], time1.format(SQL_DATE_FORMAT));
            log.info("");

            // Transaction 2: 9876543210 → 1111111111, 500,000 VND
            String txId2 = "TXN20240115002";
            LocalDateTime time2 = LocalDateTime.of(2024, 1, 15, 11, 15, 0);
            log.info("-- Transaction 2: {} → {}, 500,000 VND", accounts[1], accounts[2]);
            log.info("INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES");
            log.info("('{}', '{}', 500000.00, 0.00, '{}'),", txId2, encryptedAccounts[1], time2.format(SQL_DATE_FORMAT));
            log.info("('{}', '{}', 0.00, 500000.00, '{}');", txId2, encryptedAccounts[2], time2.format(SQL_DATE_FORMAT));
            log.info("");

            // Transaction 3: 1111111111 → 1234567890, 2,500,000 VND
            String txId3 = "TXN20240115003";
            LocalDateTime time3 = LocalDateTime.of(2024, 1, 15, 14, 20, 0);
            log.info("-- Transaction 3: {} → {}, 2,500,000 VND", accounts[2], accounts[0]);
            log.info("INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES");
            log.info("('{}', '{}', 2500000.00, 0.00, '{}'),", txId3, encryptedAccounts[2], time3.format(SQL_DATE_FORMAT));
            log.info("('{}', '{}', 0.00, 2500000.00, '{}');", txId3, encryptedAccounts[0], time3.format(SQL_DATE_FORMAT));
            log.info("");

            // Transaction 4: 2222222222 → 3333333333, 750,000 VND
            String txId4 = "TXN20240115004";
            LocalDateTime time4 = LocalDateTime.of(2024, 1, 15, 16, 45, 0);
            log.info("-- Transaction 4: {} → {}, 750,000 VND", accounts[3], accounts[4]);
            log.info("INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES");
            log.info("('{}', '{}', 750000.00, 0.00, '{}'),", txId4, encryptedAccounts[3], time4.format(SQL_DATE_FORMAT));
            log.info("('{}', '{}', 0.00, 750000.00, '{}');", txId4, encryptedAccounts[4], time4.format(SQL_DATE_FORMAT));
            log.info("");

            // Transaction 5: 3333333333 → 1234567890, 1,500,000 VND
            String txId5 = "TXN20240115005";
            LocalDateTime time5 = LocalDateTime.of(2024, 1, 15, 18, 0, 0);
            log.info("-- Transaction 5: {} → {}, 1,500,000 VND", accounts[4], accounts[0]);
            log.info("INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES");
            log.info("('{}', '{}', 1500000.00, 0.00, '{}'),", txId5, encryptedAccounts[4], time5.format(SQL_DATE_FORMAT));
            log.info("('{}', '{}', 0.00, 1500000.00, '{}');", txId5, encryptedAccounts[0], time5.format(SQL_DATE_FORMAT));
            log.info("");

        } catch (Exception e) {
            log.error("Failed to generate sample data", e);
        }
    }
}
