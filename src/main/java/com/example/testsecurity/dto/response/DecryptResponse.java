package com.example.testsecurity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response giải mã dữ liệu.
 * <p>
 * Dùng cho các endpoint:
 * <ul>
 *     <li>POST /api/encryption/rsa/decrypt</li>
 *     <li>POST /api/encryption/aes/decrypt-account</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecryptResponse {

    /**
     * Dữ liệu đã được giải mã (plaintext)
     */
    private String decrypted;
}
