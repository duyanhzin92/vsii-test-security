package com.example.testsecurity.exception;

/**
 * Enum định nghĩa các mã lỗi trong hệ thống banking transaction.
 * <p>
 * Mỗi error code đại diện cho một loại lỗi cụ thể, giúp:
 * <ul>
 *     <li>Client xử lý lỗi một cách có cấu trúc</li>
 *     <li>Logging và monitoring dễ dàng hơn</li>
 *     <li>Internationalization (i18n) trong tương lai</li>
 * </ul>
 */
public enum ErrorCode {
    /**
     * Lỗi mã hóa/giải mã
     */
    CRYPTO_ERROR("CRYPTO_ERROR", "Encryption/Decryption error"),

    /**
     * Lỗi validation dữ liệu đầu vào
     */
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation error"),

    /**
     * Tài khoản không tồn tại
     */
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Account not found"),

    /**
     * Số dư không đủ
     */
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient balance"),

    /**
     * Transaction ID đã tồn tại
     */
    DUPLICATE_TRANSACTION_ID("DUPLICATE_TRANSACTION_ID", "Duplicate transaction ID"),

    /**
     * Số tiền không hợp lệ (âm, bằng 0, quá lớn)
     */
    INVALID_AMOUNT("INVALID_AMOUNT", "Invalid amount"),

    /**
     * Lỗi nội bộ server
     */
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal server error");

    private final String code;
    private final String message;

    /**
     * Constructor
     *
     * @param code    mã lỗi
     * @param message thông báo mặc định
     */
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get mã lỗi
     *
     * @return mã lỗi
     */
    public String getCode() {
        return code;
    }

    /**
     * Get thông báo mặc định
     *
     * @return thông báo mặc định
     */
    public String getMessage() {
        return message;
    }
}
