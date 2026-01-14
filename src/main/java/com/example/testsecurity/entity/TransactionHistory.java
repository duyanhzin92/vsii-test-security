package com.example.testsecurity.entity;

import com.example.testsecurity.converter.AccountAesConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng lịch sử giao dịch trong hệ thống banking.
 * <p>
 * <b>Cấu trúc bảng:</b>
 * <ul>
 *     <li>ID: Primary key, auto increment</li>
 *     <li>TransactionID: Mã giao dịch (unique cho mỗi transaction)</li>
 *     <li>Account: Số tài khoản (đã được mã hóa AES trước khi lưu)</li>
 *     <li>InDebt: Số tiền nợ (số tiền bị trừ từ tài khoản)</li>
 *     <li>Have: Số tiền có (số tiền được cộng vào tài khoản)</li>
 *     <li>Time: Thời gian phát sinh giao dịch</li>
 * </ul>
 * <p>
 * <b>Encryption:</b>
 * <ul>
 *     <li>Account field được mã hóa bằng AES-256/GCM trước khi lưu vào database</li>
 *     <li>Các field khác (TransactionID, InDebt, Have, Time) lưu plaintext trong database</li>
 *     <li>Khi truyền giữa services, tất cả các field đều được mã hóa bằng RSA</li>
 * </ul>
 * <p>
 * <b>Business Logic:</b>
 * <ul>
 *     <li>Mỗi giao dịch chuyển khoản sẽ phát sinh 2 bản ghi:</li>
 *     <li>1. Bản ghi NỢ cho tài khoản nguồn (InDebt = amount, Have = 0)</li>
 *     <li>2. Bản ghi CÓ cho tài khoản đích (InDebt = 0, Have = amount)</li>
 * </ul>
 */
@Entity
@Table(name = "transaction_history", indexes = {
        @Index(name = "idx_transaction_id", columnList = "transactionId"),
        @Index(name = "idx_account", columnList = "account"),
        @Index(name = "idx_time", columnList = "time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    /**
     * Primary key, auto increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã giao dịch (Transaction ID)
     * <p>
     * Unique cho mỗi transaction, dùng để group các bản ghi liên quan đến cùng một giao dịch
     * <p>
     * Ví dụ: Giao dịch chuyển khoản từ A → B sẽ có 2 bản ghi với cùng TransactionID
     */
    @Column(nullable = false, length = 100)
    private String transactionId;

    /**
     * Số tài khoản (Account Number)
     * <p>
     * <b>⚠️ QUAN TRỌNG:</b> Field này được mã hóa bằng AES-256/GCM trước khi lưu vào database
     * <p>
     * Encryption được thực hiện tự động bởi JPA AttributeConverter (AccountAesConverter)
     * <p>
     * Format trong database: Base64(IV + Encrypted Account + Auth Tag)
     */
    @Convert(converter = AccountAesConverter.class)
    @Column(nullable = false, length = 500)
    private String account;

    /**
     * Số tiền nợ (Debit amount)
     * <p>
     * Số tiền bị trừ từ tài khoản trong giao dịch này
     * <p>
     * Ví dụ: Chuyển khoản 1,000,000 VND từ tài khoản A → B:
     * - Bản ghi cho tài khoản A: InDebt = 1,000,000, Have = 0
     * - Bản ghi cho tài khoản B: InDebt = 0, Have = 1,000,000
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal inDebt;

    /**
     * Số tiền có (Credit amount)
     * <p>
     * Số tiền được cộng vào tài khoản trong giao dịch này
     * <p>
     * Ví dụ: Chuyển khoản 1,000,000 VND từ tài khoản A → B:
     * - Bản ghi cho tài khoản A: InDebt = 1,000,000, Have = 0
     * - Bản ghi cho tài khoản B: InDebt = 0, Have = 1,000,000
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal have;

    /**
     * Thời gian phát sinh giao dịch
     * <p>
     * Timestamp khi giao dịch được tạo (server time)
     */
    @Column(nullable = false)
    private LocalDateTime time;
}
