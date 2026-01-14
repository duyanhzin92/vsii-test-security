package com.example.testsecurity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request mã hóa dữ liệu (AES hoặc RSA).
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
public class EncryptRequest {

    /**
     * Plaintext cần mã hóa
     */
    @NotBlank(message = "PlainText is required")
    private String plainText;
}
