package com.example.testsecurity.exception;

import com.example.testsecurity.dto.response.ApiResponse;
import com.example.testsecurity.dto.response.ErrorResponse;
import com.example.testsecurity.util.LogMaskingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler cho toàn bộ REST API layer.
 * <p>
 * <b>Nhiệm vụ:</b>
 * <ul>
 *     <li>Chuẩn hóa response lỗi trả về client</li>
 *     <li>Ẩn chi tiết kỹ thuật nội bộ, chỉ expose errorCode và message cần thiết</li>
 *     <li>Map các exception domain sang HTTP status tương ứng (404, 400, 500, ...)</li>
 *     <li><b>Che thông tin nhạy cảm trong logs:</b> TransactionID, Account, Amount, Time</li>
 * </ul>
 * <p>
 * <b>Lưu ý quan trọng:</b>
 * <ul>
 *     <li>Tất cả exceptions phải được log với masked data (che TransactionID, Account, Amount, Time)</li>
 *     <li>KHÔNG expose chi tiết kỹ thuật cho client (security risk)</li>
 *     <li>Trong production: có thể tích hợp với monitoring system (Sentry, Datadog, ...)</li>
 * </ul>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý {@link CryptoException} và trả về HTTP 400.
     * <p>
     * CryptoException xảy ra khi có lỗi trong quá trình mã hóa/giải mã (RSA, AES).
     *
     * @param ex CryptoException
     * @return ResponseEntity với ErrorResponse và status 400
     */
    @ExceptionHandler(CryptoException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleCryptoException(CryptoException ex) {
        // Log với masked data
        String maskedMessage = LogMaskingUtil.maskSensitiveData(ex.getMessage());
        log.error("CryptoException occurred: {}", maskedMessage, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.CRYPTO_ERROR)
                .message("Encryption/Decryption error occurred")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message("Encryption/Decryption error occurred")
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /**
     * Xử lý {@link BusinessException} và trả về HTTP 400.
     * <p>
     * BusinessException xảy ra khi có lỗi nghiệp vụ (số dư không đủ, tài khoản không tồn tại, ...).
     *
     * @param ex BusinessException
     * @return ResponseEntity với ErrorResponse và status 400
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException ex) {
        // Log với masked data
        String maskedMessage = LogMaskingUtil.maskSensitiveData(ex.getMessage());
        log.error("BusinessException occurred: errorCode={}, message={}",
                ex.getErrorCode(), maskedMessage, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /**
     * Fallback cho các exception khác chưa được handle riêng.
     * <p>
     * Handler này catch tất cả exception chưa được handle bởi các handler cụ thể ở trên.
     * <p>
     * <b>Lưu ý quan trọng:</b>
     * <ul>
     *     <li>Phải log đầy đủ thông tin để debug (stacktrace, context, request info)</li>
     *     <li>KHÔNG expose chi tiết kỹ thuật cho client (security risk)</li>
     *     <li>Tất cả thông tin nhạy cảm phải được che trong logs</li>
     *     <li>Trong production: có thể tích hợp với monitoring system (Sentry, Datadog, ...)</li>
     * </ul>
     *
     * @param ex exception bất kỳ chưa được handle
     * @return ResponseEntity với ErrorResponse và status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(Exception ex) {
        // Log với masked data
        String maskedMessage = LogMaskingUtil.maskSensitiveData(ex.getMessage());
        log.error("Unhandled exception occurred: type={}, message={}",
                ex.getClass().getName(), maskedMessage, ex);
        log.error("Exception stacktrace:", ex);

        // Log thông tin context nếu có
        if (ex.getCause() != null) {
            String maskedCauseMessage = LogMaskingUtil.maskSensitiveData(ex.getCause().getMessage());
            log.error("Caused by: type={}, message={}",
                    ex.getCause().getClass().getName(), maskedCauseMessage);
        }

        // Tạo error response (không expose chi tiết cho client)
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                .message("An unexpected error occurred. Please contact support if the problem persists.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message("An unexpected error occurred")
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }
}
