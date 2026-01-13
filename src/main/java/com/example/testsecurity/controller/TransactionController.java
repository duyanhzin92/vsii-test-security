package com.example.testsecurity.controller;

import com.example.testsecurity.dto.request.TransferRequest;
import com.example.testsecurity.dto.response.ApiResponse;
import com.example.testsecurity.dto.response.ErrorResponse;
import com.example.testsecurity.dto.response.TransferResponse;
import com.example.testsecurity.exception.BusinessException;
import com.example.testsecurity.exception.CryptoException;
import com.example.testsecurity.exception.ErrorCode;
import com.example.testsecurity.service.EncryptionService;
import com.example.testsecurity.service.TransactionService;
import com.example.testsecurity.util.LogMaskingUtil;
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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller xử lý các API liên quan đến banking transaction.
 * <p>
 * <b>Encryption Flow:</b>
 * <ol>
 *     <li>Client/Service: RSA encrypt TransactionID, FromAccount, ToAccount, Amount, Time với server's public key</li>
 *     <li>Controller: RSA decrypt các field này với private key</li>
 *     <li>Controller: Validate và parse dữ liệu (String → BigDecimal, String → LocalDateTime)</li>
 *     <li>Service: Xử lý nghiệp vụ (tạo 2 bản ghi: nợ và có)</li>
 *     <li>Service: AES encrypt Account trước khi lưu vào database</li>
 * </ol>
 * <p>
 * <b>Error Handling:</b>
 * <ul>
 *     <li>Tất cả exceptions được log với masked data (che TransactionID, Account, Amount, Time)</li>
 *     <li>Response được chuẩn hóa theo format ApiResponse</li>
 *     <li>Các exception cụ thể được handle riêng, không dùng catch Exception tổng quát</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction API", description = "API để xử lý giao dịch chuyển khoản ngân hàng với mã hóa dữ liệu nhạy cảm")
public class TransactionController {

    /**
     * Service để xử lý logic nghiệp vụ
     */
    private final TransactionService transactionService;

    /**
     * Service để mã hóa/giải mã dữ liệu
     */
    private final EncryptionService encryptionService;

    /**
     * DateTimeFormatter để parse time từ String
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * API endpoint để xử lý giao dịch chuyển khoản.
     * <p>
     * <b>Request Flow:</b>
     * <ol>
     *     <li>Nhận TransferRequest với tất cả field đã được RSA encrypt</li>
     *     <li>RSA decrypt các field: TransactionID, FromAccount, ToAccount, Amount, Time</li>
     *     <li>Validate và parse dữ liệu (String → BigDecimal, String → LocalDateTime)</li>
     *     <li>Gọi TransactionService để xử lý nghiệp vụ</li>
     *     <li>Trả về TransferResponse với status SUCCESS</li>
     * </ol>
     * <p>
     * <b>Error Handling:</b>
     * <ul>
     *     <li>CryptoException: RSA decryption failed → HTTP 400</li>
     *     <li>BusinessException: Business logic error → HTTP 400</li>
     *     <li>NumberFormatException: Invalid amount format → HTTP 400</li>
     *     <li>DateTimeParseException: Invalid time format → HTTP 400</li>
     *     <li>IllegalArgumentException: Invalid input → HTTP 400</li>
     *     <li>NullPointerException: Null value → HTTP 500</li>
     * </ul>
     *
     * @param request TransferRequest với tất cả field đã được RSA encrypt
     * @return ResponseEntity với TransferResponse
     */
    @PostMapping("/transfer")
    @Operation(
            summary = "Xử lý giao dịch chuyển khoản",
            description = "API để xử lý giao dịch chuyển khoản giữa 2 tài khoản. " +
                    "Tất cả các field trong request (transactionId, fromAccount, toAccount, amount, time) " +
                    "phải được mã hóa bằng RSA với server's public key trước khi gửi. " +
                    "Mỗi giao dịch sẽ tạo 2 bản ghi: 1 bản ghi NỢ cho tài khoản nguồn và 1 bản ghi CÓ cho tài khoản đích."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giao dịch được xử lý thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Lỗi validation hoặc nghiệp vụ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi nội bộ server",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        try {
            log.info("Received transfer request");

            // Step 1: RSA decrypt các field từ request
            String transactionId = decryptField(request.getTransactionId(), "TransactionID");
            String fromAccount = decryptField(request.getFromAccount(), "FromAccount");
            String toAccount = decryptField(request.getToAccount(), "ToAccount");
            BigDecimal amount = parseAmount(decryptField(request.getAmount(), "Amount"));
            LocalDateTime time = parseTime(decryptField(request.getTime(), "Time"));

            // Step 2: Gọi service để xử lý nghiệp vụ
            transactionService.processTransfer(transactionId, fromAccount, toAccount, amount, time);

            // Step 3: Tạo response
            TransferResponse response = TransferResponse.builder()
                    .transactionId(transactionId)
                    .status("SUCCESS")
                    .message("Transfer transaction processed successfully")
                    .build();

            log.info("Transfer transaction completed successfully: transactionId={}",
                    LogMaskingUtil.maskTransactionId(transactionId));

            return ResponseEntity.ok(ApiResponse.<TransferResponse>builder()
                    .success(true)
                    .message("Transfer transaction processed successfully")
                    .data(response)
                    .timestamp(java.time.LocalDateTime.now())
                    .build());

        } catch (CryptoException e) {
            log.error("Crypto error in transfer request: {}", LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.CRYPTO_ERROR,
                    "Encryption/Decryption error: " + e.getMessage()
            );

        } catch (BusinessException e) {
            log.error("Business error in transfer request: {}", LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    e.getErrorCode(),
                    e.getMessage()
            );

        } catch (NumberFormatException e) {
            log.error("Number format error in transfer request: {}", LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INVALID_AMOUNT,
                    "Invalid amount format: " + e.getMessage()
            );

        } catch (DateTimeParseException e) {
            log.error("Date time parse error in transfer request: {}", LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid time format. Expected ISO-8601 format (e.g., 2024-01-15T10:30:00)"
            );

        } catch (IllegalArgumentException e) {
            log.error("Illegal argument error in transfer request: {}", LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid input: " + e.getMessage()
            );

        } catch (NullPointerException e) {
            log.error("Null pointer error in transfer request: {}", LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            return buildErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Null value detected. Please check your request data."
            );
        }
    }

    /**
     * API endpoint để lấy RSA Public Key.
     * <p>
     * Client/Service cần public key này để mã hóa dữ liệu trước khi gửi lên server.
     * <p>
     * <b>Lưu ý:</b> Public key này dùng để mã hóa các field trong TransferRequest (RSA encryption)
     *
     * @return ResponseEntity với RSA Public Key (Base64 encoded)
     */
    @GetMapping("/public-key")
    @Operation(
            summary = "Lấy RSA Public Key",
            description = "API để lấy RSA Public Key của server. " +
                    "Client/Service cần public key này để mã hóa các field (transactionId, fromAccount, toAccount, amount, time) " +
                    "trước khi gửi request đến API /transfer. " +
                    "Public key được trả về dưới dạng Base64 encoded string."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy public key thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi khi lấy public key",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<String>> getPublicKey() {
        try {
            String publicKey = encryptionService.getRSAPublicKeyBase64();
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("RSA Public Key retrieved successfully")
                    .data(publicKey)
                    .timestamp(java.time.LocalDateTime.now())
                    .build());

        } catch (CryptoException e) {
            log.error("Crypto error when getting public key: {}", e.getMessage(), e);
            return buildStringErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.CRYPTO_ERROR,
                    "Failed to retrieve RSA public key: " + e.getMessage()
            );

        } catch (IllegalStateException e) {
            log.error("Illegal state error when getting public key: {}", e.getMessage(), e);
            return buildStringErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Encryption service is not properly initialized"
            );

        } catch (NullPointerException e) {
            log.error("Null pointer error when getting public key", e);
            return buildStringErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Encryption service returned null public key"
            );
        }
    }

    /**
     * RSA decrypt một field từ request
     *
     * @param encryptedField Field đã được RSA encrypt (Base64 encoded)
     * @param fieldName      Tên field (để log error message)
     * @return Field đã được decrypt (plaintext)
     * @throws CryptoException nếu RSA decryption failed
     * @throws IllegalArgumentException nếu encryptedField null hoặc empty
     */
    private String decryptField(String encryptedField, String fieldName) {
        if (encryptedField == null || encryptedField.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        try {
            return encryptionService.decryptRSA(encryptedField);
        } catch (CryptoException e) {
            log.error("Failed to decrypt {}: {}", fieldName, e.getMessage(), e);
            throw new CryptoException("Failed to decrypt " + fieldName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Parse amount từ String sang BigDecimal
     *
     * @param amountString Số tiền dạng String (sau khi RSA decrypt)
     * @return BigDecimal
     * @throws BusinessException nếu parse failed hoặc amount không hợp lệ
     * @throws NumberFormatException nếu format không hợp lệ
     */
    private BigDecimal parseAmount(String amountString) {
        if (amountString == null || amountString.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "Amount is required");
        }

        try {
            BigDecimal amount = new BigDecimal(amountString);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.INVALID_AMOUNT, "Amount must be greater than 0");
            }
            return amount;
        } catch (NumberFormatException e) {
            log.error("Failed to parse amount: {}", LogMaskingUtil.maskAmount(amountString), e);
            throw new NumberFormatException("Invalid amount format: " + amountString);
        }
    }

    /**
     * Parse time từ String sang LocalDateTime
     *
     * @param timeString Thời gian dạng String (sau khi RSA decrypt, format: ISO-8601)
     * @return LocalDateTime
     * @throws BusinessException nếu parse failed
     * @throws DateTimeParseException nếu format không hợp lệ
     */
    private LocalDateTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Time is required");
        }

        try {
            return LocalDateTime.parse(timeString, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse time: {}", LogMaskingUtil.maskTime(timeString), e);
            throw new DateTimeParseException("Invalid time format. Expected ISO-8601 format (e.g., 2024-01-15T10:30:00)", timeString, 0);
        }
    }

    /**
     * Build error response cho TransferResponse
     *
     * @param httpStatus HTTP status code
     * @param errorCode  Error code
     * @param message    Error message
     * @return ResponseEntity với ErrorResponse
     */
    private ResponseEntity<ApiResponse<TransferResponse>> buildErrorResponse(
            HttpStatus httpStatus,
            ErrorCode errorCode,
            String message) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .build();

        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.<TransferResponse>builder()
                        .success(false)
                        .message(message)
                        .data(null)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /**
     * Build error response cho String response (dùng cho getPublicKey)
     *
     * @param httpStatus HTTP status code
     * @param errorCode  Error code
     * @param message    Error message
     * @return ResponseEntity với ErrorResponse
     */
    private ResponseEntity<ApiResponse<String>> buildStringErrorResponse(
            HttpStatus httpStatus,
            ErrorCode errorCode,
            String message) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .build();

        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.<String>builder()
                        .success(false)
                        .message(message)
                        .data(null)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /**
     * Handle validation errors từ @Valid annotation
     *
     * @param ex MethodArgumentNotValidException
     * @return ResponseEntity với ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Validation failed")
                .details(errors)
                .build();

        log.warn("Validation error: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }
}
