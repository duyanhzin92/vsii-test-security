package com.example.testsecurity.exception;

/**
 * Exception nghiệp vụ cho banking transaction system.
 * <p>
 * Dùng cho các lỗi nghiệp vụ như:
 * <ul>
 *     <li>Số dư không đủ</li>
 *     <li>Tài khoản không tồn tại</li>
 *     <li>Transaction ID đã tồn tại</li>
 *     <li>Số tiền không hợp lệ</li>
 * </ul>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Constructor với error code và message
     *
     * @param errorCode mã lỗi nghiệp vụ
     * @param message   thông báo lỗi
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor với error code, message và cause
     *
     * @param errorCode mã lỗi nghiệp vụ
     * @param message   thông báo lỗi
     * @param cause     exception gốc
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Get error code
     *
     * @return error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
