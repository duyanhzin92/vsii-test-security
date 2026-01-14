package com.example.testsecurity.controller;

import com.example.testsecurity.dto.request.DecryptRequest;
import com.example.testsecurity.dto.request.EncryptRequest;
import com.example.testsecurity.dto.response.ApiResponse;
import com.example.testsecurity.dto.response.DecryptResponse;
import com.example.testsecurity.dto.response.EncryptResponse;
import com.example.testsecurity.exception.CryptoException;
import com.example.testsecurity.exception.ErrorCode;
import com.example.testsecurity.service.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để test và demo các chức năng mã hóa AES và RSA.
 * <p>
 * <b>Mục đích:</b>
 * <ul>
 *     <li>Test RSA encryption để mã hóa dữ liệu trước khi gửi lên server</li>
 *     <li>Test AES encryption để mã hóa Account number</li>
 *     <li>Demo encryption flow cho developers</li>
 * </ul>
 * <p>
 * <b>Lưu ý:</b> Các endpoint này chỉ dùng cho development/testing, không nên enable trong production
 */
@RestController
@RequestMapping("/api/encryption")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Encryption Test API", description = "APIs để test mã hóa AES và RSA (chỉ dùng cho development/testing)")
public class EncryptionController {

    private final EncryptionService encryptionService;

    @org.springframework.beans.factory.annotation.Value("${encryption.aes.key}")
    private String aesKeyBase64;

    /**
     * Mã hóa dữ liệu bằng RSA Public Key
     * <p>
     * Endpoint này dùng để test RSA encryption trước khi gửi dữ liệu lên server.
     * <p>
     * <b>Ví dụ sử dụng:</b>
     * <ol>
     *     <li>Gọi GET /api/transactions/public-key để lấy RSA public key</li>
     *     <li>Gọi POST /api/encryption/rsa/encrypt với plaintext để mã hóa</li>
     *     <li>Sử dụng encrypted value để gửi lên POST /api/transactions/transfer</li>
     * </ol>
     *
     * @param request Request chứa plaintext cần mã hóa
     * @return Response chứa encrypted data (Base64 encoded)
     */
    @PostMapping("/rsa/encrypt")
    @Operation(
            summary = "Mã hóa RSA",
            description = "Mã hóa dữ liệu bằng RSA public key. " +
                    "Dùng để test RSA encryption trước khi gửi dữ liệu lên server. " +
                    "Chỉ dùng cho dữ liệu nhỏ (≤ 245 bytes cho RSA-2048). " +
                    "Các field như TransactionID, Account, Amount, Time đều có thể mã hóa bằng endpoint này."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mã hóa thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Lỗi mã hóa (data quá lớn, invalid input, ...)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<EncryptResponse>> encryptRSA(@Valid @RequestBody EncryptRequest request) {
        try {
            String encrypted = encryptionService.encryptRSA(request.getPlainText());
            log.info("RSA encryption successful (data length: {})", request.getPlainText().length());

            return ResponseEntity.ok(ApiResponse.<EncryptResponse>builder()
                    .success(true)
                    .message("RSA encryption successful")
                    .data(new EncryptResponse(encrypted))
                    .timestamp(java.time.LocalDateTime.now())
                    .build());

        } catch (CryptoException e) {
            log.error("RSA encryption failed: {}", e.getMessage(), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.CRYPTO_ERROR,
                    "RSA encryption failed: " + e.getMessage()
            );
        }
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key
     * <p>
     * Endpoint này dùng để test RSA decryption (server-side).
     * <p>
     * <b>Lưu ý:</b> Trong thực tế, server sẽ tự động decrypt khi nhận request từ client.
     *
     * @param request Request chứa encrypted data (Base64 encoded)
     * @return Response chứa decrypted plaintext
     */
    @PostMapping("/rsa/decrypt")
    @Operation(
            summary = "Giải mã RSA",
            description = "Giải mã dữ liệu bằng RSA private key. " +
                    "Dùng để test RSA decryption. " +
                    "Trong thực tế, server sẽ tự động decrypt khi nhận request từ client."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giải mã thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Lỗi giải mã (invalid key, corrupted data, ...)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DecryptResponse>> decryptRSA(@Valid @RequestBody DecryptRequest request) {
        try {
            String decrypted = encryptionService.decryptRSA(request.getCipherText());
            log.info("RSA decryption successful");

            return ResponseEntity.ok(ApiResponse.<DecryptResponse>builder()
                    .success(true)
                    .message("RSA decryption successful")
                    .data(new DecryptResponse(decrypted))
                    .timestamp(java.time.LocalDateTime.now())
                    .build());

        } catch (CryptoException e) {
            log.error("RSA decryption failed: {}", e.getMessage(), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.CRYPTO_ERROR,
                    "RSA decryption failed: " + e.getMessage()
            );
        }
    }

    /**
     * Mã hóa Account Number bằng AES (để lưu vào database)
     * <p>
     * Endpoint này dùng để test AES encryption cho Account number.
     * <p>
     * <b>Lưu ý:</b> Trong thực tế, Account sẽ được tự động mã hóa khi lưu vào database.
     *
     * @param request Request chứa Account number (plaintext)
     * @return Response chứa encrypted Account (Base64 encoded)
     */
    @PostMapping("/aes/encrypt-account")
    @Operation(
            summary = "Mã hóa Account bằng AES",
            description = "Mã hóa Account Number bằng AES-256/GCM để lưu vào database. " +
                    "Dùng để test AES encryption. " +
                    "Trong thực tế, Account sẽ được tự động mã hóa khi lưu vào database."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mã hóa thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Lỗi mã hóa",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<EncryptResponse>> encryptAccount(@Valid @RequestBody EncryptRequest request) {
        try {
            String encrypted = com.example.testsecurity.util.AesUtil.encrypt(
                    request.getPlainText(),
                    com.example.testsecurity.util.AesUtil.keyFromString(aesKeyBase64)
            );
            log.info("AES encryption successful for account (length: {})", request.getPlainText().length());

            return ResponseEntity.ok(ApiResponse.<EncryptResponse>builder()
                    .success(true)
                    .message("AES encryption successful")
                    .data(new EncryptResponse(encrypted))
                    .timestamp(java.time.LocalDateTime.now())
                    .build());

        } catch (CryptoException e) {
            log.error("AES encryption failed: {}", e.getMessage(), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.CRYPTO_ERROR,
                    "AES encryption failed: " + e.getMessage()
            );
        }
    }

    /**
     * Giải mã Account Number từ database
     * <p>
     * Endpoint này dùng để test AES decryption cho Account number đã được mã hóa trong database.
     *
     * @param request Request chứa encrypted Account (Base64 encoded)
     * @return Response chứa decrypted Account number
     */
    @PostMapping("/aes/decrypt-account")
    @Operation(
            summary = "Giải mã Account từ database",
            description = "Giải mã Account Number đã được mã hóa AES từ database. " +
                    "Dùng để test AES decryption."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giải mã thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Lỗi giải mã",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<DecryptResponse>> decryptAccount(@Valid @RequestBody DecryptRequest request) {
        try {
            String decrypted = com.example.testsecurity.util.AesUtil.decrypt(
                    request.getCipherText(),
                    com.example.testsecurity.util.AesUtil.keyFromString(aesKeyBase64)
            );
            log.info("AES decryption successful for account");

            return ResponseEntity.ok(ApiResponse.<DecryptResponse>builder()
                    .success(true)
                    .message("AES decryption successful")
                    .data(new DecryptResponse(decrypted))
                    .timestamp(java.time.LocalDateTime.now())
                    .build());

        } catch (CryptoException e) {
            log.error("AES decryption failed: {}", e.getMessage(), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.CRYPTO_ERROR,
                    "AES decryption failed: " + e.getMessage()
            );
        }
    }

    /**
     * Build error response
     */
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(
            HttpStatus httpStatus,
            ErrorCode errorCode,
            String message) {

        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.<T>builder()
                        .success(false)
                        .message(message)
                        .data(null)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }
}
