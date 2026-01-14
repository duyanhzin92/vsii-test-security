package com.example.testsecurity.constants;

/**
 * Constants cho Transaction module.
 * <p>
 * Chứa tất cả các constants liên quan đến transaction processing:
 * <ul>
 *     <li>Status values</li>
 *     <li>Field names</li>
 *     <li>Error messages</li>
 *     <li>Log messages</li>
 * </ul>
 */
public final class TransactionConstants {

    /**
     * Private constructor để ngăn instantiation
     */
    private TransactionConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // ============ Transaction Status ============

    /**
     * Trạng thái giao dịch thành công
     */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * Trạng thái giao dịch thất bại
     */
    public static final String STATUS_FAILED = "FAILED";

    // ============ Field Names ============

    /**
     * Tên field Transaction ID
     */
    public static final String FIELD_TRANSACTION_ID = "TransactionID";

    /**
     * Tên field From Account
     */
    public static final String FIELD_FROM_ACCOUNT = "FromAccount";

    /**
     * Tên field To Account
     */
    public static final String FIELD_TO_ACCOUNT = "ToAccount";

    /**
     * Tên field Amount
     */
    public static final String FIELD_AMOUNT = "Amount";

    /**
     * Tên field Time
     */
    public static final String FIELD_TIME = "Time";

    // ============ Messages ============

    /**
     * Message khi transfer transaction thành công
     */
    public static final String MSG_TRANSFER_SUCCESS = "Transfer transaction processed successfully";

    /**
     * Message khi lấy RSA public key thành công
     */
    public static final String MSG_PUBLIC_KEY_RETRIEVED = "RSA Public Key retrieved successfully";

    /**
     * Message khi validation failed
     */
    public static final String MSG_VALIDATION_FAILED = "Validation failed";

    /**
     * Message khi có lỗi không mong đợi
     */
    public static final String MSG_UNEXPECTED_ERROR = "An unexpected error occurred. Please contact support if the problem persists.";

    /**
     * Message khi có lỗi nội bộ server
     */
    public static final String MSG_INTERNAL_SERVER_ERROR = "An unexpected error occurred";

    // ============ Error Messages ============

    /**
     * Error message: Transaction ID is required
     */
    public static final String ERR_TRANSACTION_ID_REQUIRED = "transactionId rỗng";

    /**
     * Error message: From account is required
     */
    public static final String ERR_FROM_ACCOUNT_REQUIRED = "fromAccount rỗng";

    /**
     * Error message: To account is required
     */
    public static final String ERR_TO_ACCOUNT_REQUIRED = "toAccount rỗng";

    /**
     * Error message: Amount is required
     */
    public static final String ERR_AMOUNT_REQUIRED = "amount rỗng";

    /**
     * Error message: Time is required
     */
    public static final String ERR_TIME_REQUIRED = "time rỗng";

    /**
     * Error message: From account and To account cannot be the same
     */
    public static final String ERR_ACCOUNTS_CANNOT_BE_SAME = "From account and To account cannot be the same";

    /**
     * Error message: Amount must be greater than 0
     */
    public static final String ERR_AMOUNT_MUST_BE_GREATER_THAN_ZERO = "amount phải > 0";

    /**
     * Error message: Transaction ID already exists
     */
    public static final String ERR_TRANSACTION_ID_ALREADY_EXISTS = "Transaction ID already exists";

    /**
     * Error message: Invalid time format
     */
    public static final String ERR_INVALID_TIME_FORMAT = "Invalid time format. Expected ISO-8601 format (e.g., 2024-01-15T10:30:00)";

    /**
     * Error message: Invalid amount format
     */
    public static final String ERR_INVALID_AMOUNT_FORMAT = "Invalid amount format";

    /**
     * Error message: Encryption/Decryption error
     */
    public static final String ERR_CRYPTO_ERROR = "Encryption/Decryption error";

    /**
     * Error message: Failed to retrieve RSA public key
     */
    public static final String ERR_FAILED_TO_RETRIEVE_PUBLIC_KEY = "Failed to retrieve RSA public key";

    /**
     * Error message: Encryption service is not properly initialized
     */
    public static final String ERR_ENCRYPTION_SERVICE_NOT_INITIALIZED = "Encryption service is not properly initialized";

    /**
     * Error message: Encryption service returned null public key
     */
    public static final String ERR_NULL_PUBLIC_KEY = "Encryption service returned null public key";

    /**
     * Error message: Null value detected
     */
    public static final String ERR_NULL_VALUE_DETECTED = "Null value detected. Please check your request data.";

    /**
     * Error message: Failed to create debit record
     */
    public static final String ERR_FAILED_TO_CREATE_DEBIT_RECORD = "Failed to create debit record";

    /**
     * Error message: Failed to create credit record
     */
    public static final String ERR_FAILED_TO_CREATE_CREDIT_RECORD = "Failed to create credit record";

    /**
     * Error message: Unexpected error while processing transfer transaction
     */
    public static final String ERR_UNEXPECTED_ERROR_PROCESSING_TRANSFER = "An unexpected error occurred while processing transfer transaction";

    // ============ Log Messages ============

    /**
     * Log message: Received transfer request
     */
    public static final String LOG_RECEIVED_TRANSFER_REQUEST = "Received transfer request";

    /**
     * Log message: Transfer transaction completed successfully
     */
    public static final String LOG_TRANSFER_COMPLETED_SUCCESS = "Transfer transaction completed successfully: transactionId={}";

    /**
     * Log message: Processing transfer transaction
     */
    public static final String LOG_PROCESSING_TRANSFER = "Processing transfer transaction: transactionId={}, fromAccount={}, toAccount={}, amount={}, time={}";

    /**
     * Log message: Transfer transaction processed successfully
     */
    public static final String LOG_TRANSFER_PROCESSED_SUCCESS = "Transfer transaction processed successfully: transactionId={}";

    /**
     * Log message: Duplicate transaction ID detected
     */
    public static final String LOG_DUPLICATE_TRANSACTION_ID = "Duplicate transaction ID detected: transactionId={}";

    /**
     * Log message: Crypto error in transfer request
     */
    public static final String LOG_CRYPTO_ERROR_TRANSFER = "Crypto error in transfer request: {}";

    /**
     * Log message: Business error in transfer request
     */
    public static final String LOG_BUSINESS_ERROR_TRANSFER = "Business error in transfer request: {}";

    /**
     * Log message: Number format error in transfer request
     */
    public static final String LOG_NUMBER_FORMAT_ERROR = "Number format error in transfer request: {}";

    /**
     * Log message: Date time parse error in transfer request
     */
    public static final String LOG_DATETIME_PARSE_ERROR = "Date time parse error in transfer request: {}";

    /**
     * Log message: Illegal argument error in transfer request
     */
    public static final String LOG_ILLEGAL_ARGUMENT_ERROR = "Illegal argument error in transfer request: {}";

    /**
     * Log message: Null pointer error in transfer request
     */
    public static final String LOG_NULL_POINTER_ERROR = "Null pointer error in transfer request: {}";

    /**
     * Log message: Unexpected error processing transfer transaction
     */
    public static final String LOG_UNEXPECTED_ERROR_PROCESSING = "Unexpected error processing transfer transaction: transactionId={}, fromAccount={}, toAccount={}, amount={}, time={}";

    /**
     * Log message: Failed to decrypt field
     */
    public static final String LOG_FAILED_TO_DECRYPT_FIELD = "Failed to decrypt {}: {}";

    /**
     * Log message: Failed to parse amount
     */
    public static final String LOG_FAILED_TO_PARSE_AMOUNT = "Failed to parse amount: {}";

    /**
     * Log message: Failed to parse time
     */
    public static final String LOG_FAILED_TO_PARSE_TIME = "Failed to parse time: {}";

    /**
     * Log message: Debit record created
     */
    public static final String LOG_DEBIT_RECORD_CREATED = "Debit record created: transactionId={}, account={}";

    /**
     * Log message: Credit record created
     */
    public static final String LOG_CREDIT_RECORD_CREATED = "Credit record created: transactionId={}, account={}";

    /**
     * Log message: Failed to encrypt account for debit record
     */
    public static final String LOG_FAILED_TO_ENCRYPT_ACCOUNT_DEBIT = "Failed to encrypt account for debit record: transactionId={}, account={}";

    /**
     * Log message: Failed to encrypt account for credit record
     */
    public static final String LOG_FAILED_TO_ENCRYPT_ACCOUNT_CREDIT = "Failed to encrypt account for credit record: transactionId={}, account={}";

    /**
     * Log message: Failed to create debit record
     */
    public static final String LOG_FAILED_TO_CREATE_DEBIT = "Failed to create debit record: transactionId={}, account={}, amount={}, time={}";

    /**
     * Log message: Failed to create credit record
     */
    public static final String LOG_FAILED_TO_CREATE_CREDIT = "Failed to create credit record: transactionId={}, account={}, amount={}, time={}";

    /**
     * Log message: Validating business rules
     */
    public static final String LOG_VALIDATING_BUSINESS_RULES = "Validating business rules for transfer: fromAccount={}, toAccount={}, amount={}";
}
