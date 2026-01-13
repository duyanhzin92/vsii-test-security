package com.example.testsecurity.exception;

/**
 * Exception được throw khi có lỗi trong quá trình mã hóa/giải mã.
 * <p>
 * Các trường hợp thường gặp:
 * <ul>
 *     <li>Key không hợp lệ (format sai, key bị corrupt)</li>
 *     <li>Dữ liệu bị tamper (AES GCM tag không khớp)</li>
 *     <li>Dữ liệu quá lớn cho RSA encryption</li>
 *     <li>Base64 format không hợp lệ</li>
 * </ul>
 */
public class CryptoException extends RuntimeException {

    /**
     * Constructor với message
     *
     * @param message thông báo lỗi
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     *
     * @param message thông báo lỗi
     * @param cause   exception gốc
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
