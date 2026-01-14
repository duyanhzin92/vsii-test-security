package com.example.testsecurity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request giải mã dữ liệu (AES hoặc RSA).
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
public class DecryptRequest {

    /**
     * Ciphertext cần giải mã (Base64 encoded)
     */
    @NotBlank(message = "CipherText is required")
    private String cipherText;
}
