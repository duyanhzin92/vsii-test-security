package com.example.testsecurity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response chuyển khoản.
 * <p>
 * Response này được trả về sau khi xử lý thành công giao dịch chuyển khoản.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    /**
     * Mã giao dịch (Transaction ID)
     * <p>
     * Mã giao dịch đã được xử lý thành công
     */
    private String transactionId;

    /**
     * Trạng thái giao dịch
     * <p>
     * "SUCCESS" nếu giao dịch thành công
     */
    private String status;

    /**
     * Thông báo
     */
    private String message;
}
