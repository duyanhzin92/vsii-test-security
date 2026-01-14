package com.example.testsecurity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response mã hóa dữ liệu.
 * <p>
 * Dùng cho các endpoint:
 * <ul>
 *     <li>POST /api/encryption/rsa/encrypt</li>
 *     <li>POST /api/encryption/aes/encrypt-account</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptResponse {

    /**
     * Dữ liệu đã được mã hóa (Base64 encoded)
     */
    private String encrypted;
}
