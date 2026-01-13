package com.example.testsecurity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request chuyển khoản.
 * <p>
 * <b>⚠️ QUAN TRỌNG:</b> Tất cả các field trong request này đều được mã hóa bằng RSA
 * trước khi gửi từ client/service khác.
 * <p>
 * <b>Encryption Flow:</b>
 * <ol>
 *     <li>Client/Service: RSA encrypt TransactionID, FromAccount, ToAccount, Amount, Time với server's public key</li>
 *     <li>Server: RSA decrypt các field này với private key</li>
 *     <li>Server: Xử lý nghiệp vụ (tạo 2 bản ghi: nợ cho tài khoản nguồn, có cho tài khoản đích)</li>
 *     <li>Server: AES encrypt Account trước khi lưu vào database</li>
 * </ol>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    /**
     * Mã giao dịch (Transaction ID)
     * <p>
     * <b>Encrypted:</b> Đã được mã hóa bằng RSA (Base64 encoded)
     * <p>
     * Server sẽ decrypt field này trước khi sử dụng
     */
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    /**
     * Số tài khoản nguồn (From Account)
     * <p>
     * <b>Encrypted:</b> Đã được mã hóa bằng RSA (Base64 encoded)
     * <p>
     * Server sẽ decrypt field này, sau đó AES encrypt trước khi lưu vào database
     */
    @NotBlank(message = "From account is required")
    private String fromAccount;

    /**
     * Số tài khoản đích (To Account)
     * <p>
     * <b>Encrypted:</b> Đã được mã hóa bằng RSA (Base64 encoded)
     * <p>
     * Server sẽ decrypt field này, sau đó AES encrypt trước khi lưu vào database
     */
    @NotBlank(message = "To account is required")
    private String toAccount;

    /**
     * Số tiền chuyển khoản
     * <p>
     * <b>Encrypted:</b> Đã được mã hóa bằng RSA (Base64 encoded)
     * <p>
     * Format sau khi decrypt: String representation của số tiền (ví dụ: "1000000.50")
     * <p>
     * Server sẽ decrypt và convert sang BigDecimal
     */
    @NotBlank(message = "Amount is required")
    private String amount;

    /**
     * Thời gian phát sinh giao dịch
     * <p>
     * <b>Encrypted:</b> Đã được mã hóa bằng RSA (Base64 encoded)
     * <p>
     * Format sau khi decrypt: ISO-8601 format (ví dụ: "2024-01-15T10:30:00")
     * <p>
     * Server sẽ decrypt và parse sang LocalDateTime
     */
    @NotBlank(message = "Time is required")
    private String time;
}
