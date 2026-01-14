package com.example.testsecurity.constants;

/**
 * Constants cho Encryption module.
 * <p>
 * Chứa tất cả các constants liên quan đến encryption:
 * <ul>
 *     <li>Algorithm names</li>
 *     <li>Key sizes</li>
 *     <li>Error messages</li>
 *     <li>Log messages</li>
 * </ul>
 */
public final class EncryptionConstants {

    /**
     * Private constructor để ngăn instantiation
     */
    private EncryptionConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // ============ Algorithm Names ============

    /**
     * AES algorithm name
     */
    public static final String ALGORITHM_AES = "AES";

    /**
     * AES/GCM/NoPadding algorithm
     */
    public static final String ALGORITHM_AES_GCM = "AES/GCM/NoPadding";

    /**
     * RSA algorithm name
     */
    public static final String ALGORITHM_RSA = "RSA";

    /**
     * RSA/ECB/PKCS1Padding algorithm
     */
    public static final String ALGORITHM_RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding";

    // ============ Key Sizes ============

    /**
     * AES key size: 256-bit
     */
    public static final int AES_KEY_SIZE = 256;

    /**
     * RSA key size: 2048-bit
     */
    public static final int RSA_KEY_SIZE = 2048;

    /**
     * GCM IV length: 12 bytes (96-bit)
     */
    public static final int GCM_IV_LENGTH = 12;

    /**
     * GCM authentication tag length: 128-bit
     */
    public static final int GCM_TAG_LENGTH = 128;

    /**
     * RSA max data size for encryption: ~245 bytes (for RSA-2048)
     */
    public static final int RSA_MAX_DATA_SIZE = 245;

    // ============ Error Messages ============

    /**
     * Error message: AES/GCM algorithm not supported
     */
    public static final String ERR_AES_ALGORITHM_NOT_SUPPORTED = "AES/GCM algorithm not supported by JVM";

    /**
     * Error message: AES/GCM encryption failed
     */
    public static final String ERR_AES_ENCRYPTION_FAILED = "AES/GCM encryption failed";

    /**
     * Error message: AES/GCM decryption failed
     */
    public static final String ERR_AES_DECRYPTION_FAILED = "AES/GCM decryption failed";

    /**
     * Error message: Ciphertext too short
     */
    public static final String ERR_CIPHERTEXT_TOO_SHORT = "Ciphertext too short (invalid format)";

    /**
     * Error message: Invalid Base64 ciphertext format
     */
    public static final String ERR_INVALID_BASE64_CIPHERTEXT = "Invalid Base64 ciphertext format";

    /**
     * Error message: Invalid AES key or tampered ciphertext
     */
    public static final String ERR_INVALID_AES_KEY_OR_TAMPERED = "Invalid AES key or tampered ciphertext (authentication failed)";

    /**
     * Error message: Generate AES key failed
     */
    public static final String ERR_GENERATE_AES_KEY_FAILED = "Generate AES key failed: AES algorithm not supported";

    /**
     * Error message: Invalid RSA public key
     */
    public static final String ERR_INVALID_RSA_PUBLIC_KEY = "Invalid RSA public key";

    /**
     * Error message: Data too large for RSA encryption
     */
    public static final String ERR_DATA_TOO_LARGE_FOR_RSA = "Data too large for RSA encryption (max ~245 bytes for RSA-2048)";

    /**
     * Error message: RSA encryption failed
     */
    public static final String ERR_RSA_ENCRYPTION_FAILED = "RSA encryption failed";

    /**
     * Error message: Invalid Base64 ciphertext
     */
    public static final String ERR_INVALID_BASE64_CIPHERTEXT_RSA = "Invalid Base64 ciphertext";

    /**
     * Error message: Invalid RSA private key or corrupted ciphertext
     */
    public static final String ERR_INVALID_RSA_PRIVATE_KEY = "Invalid RSA private key or corrupted ciphertext";

    /**
     * Error message: RSA decryption failed
     */
    public static final String ERR_RSA_DECRYPTION_FAILED = "RSA decryption failed";

    /**
     * Error message: Generate RSA key pair failed
     */
    public static final String ERR_GENERATE_RSA_KEY_PAIR_FAILED = "Generate RSA key pair failed: RSA algorithm not supported";

    /**
     * Error message: Invalid AES key format in config
     */
    public static final String ERR_INVALID_AES_KEY_FORMAT = "Invalid AES key format in config";

    /**
     * Error message: Invalid Base64 format for RSA public key
     */
    public static final String ERR_INVALID_BASE64_RSA_PUBLIC_KEY = "Invalid Base64 format for RSA public key";

    /**
     * Error message: Invalid RSA public key specification
     */
    public static final String ERR_INVALID_RSA_PUBLIC_KEY_SPEC = "Invalid RSA public key specification";

    /**
     * Error message: Invalid Base64 format for RSA private key
     */
    public static final String ERR_INVALID_BASE64_RSA_PRIVATE_KEY = "Invalid Base64 format for RSA private key";

    /**
     * Error message: Invalid RSA private key specification
     */
    public static final String ERR_INVALID_RSA_PRIVATE_KEY_SPEC = "Invalid RSA private key specification";

    /**
     * Error message: RSA algorithm not supported
     */
    public static final String ERR_RSA_ALGORITHM_NOT_SUPPORTED = "RSA algorithm not supported by JVM";

    /**
     * Error message: Failed to generate temporary RSA keys
     */
    public static final String ERR_FAILED_TO_GENERATE_TEMP_RSA_KEYS = "Failed to generate temporary RSA keys";

    /**
     * Error message: Invalid account format for AES encryption
     */
    public static final String ERR_INVALID_ACCOUNT_FORMAT_AES = "Invalid account format for AES encryption";

    /**
     * Error message: Invalid input data for AES encryption
     */
    public static final String ERR_INVALID_INPUT_DATA_AES = "Invalid input data for AES encryption";

    /**
     * Error message: Invalid input data for RSA encryption
     */
    public static final String ERR_INVALID_INPUT_DATA_RSA = "Invalid input data for RSA encryption";

    // ============ Log Messages ============

    /**
     * Log message: AES key loaded successfully
     */
    public static final String LOG_AES_KEY_LOADED = "AES key loaded successfully from config";

    /**
     * Log message: RSA keys loaded successfully
     */
    public static final String LOG_RSA_KEYS_LOADED = "RSA keys loaded successfully from config";

    /**
     * Log message: EncryptionService initialized successfully
     */
    public static final String LOG_ENCRYPTION_SERVICE_INITIALIZED = "EncryptionService initialized successfully";

    /**
     * Log message: Generating temporary RSA key pair
     */
    public static final String LOG_GENERATING_TEMP_RSA_KEYS = "Generating temporary RSA key pair (2048-bit)";

    /**
     * Log message: Temporary RSA key pair generated successfully
     */
    public static final String LOG_TEMP_RSA_KEYS_GENERATED = "Temporary RSA key pair generated successfully";

    /**
     * Log message: RSA keys not found in config
     */
    public static final String LOG_RSA_KEYS_NOT_FOUND = "RSA keys not found in config, generating temporary keys. Run RsaKeyGenerator to generate real keys for production!";

    /**
     * Log message: Encrypting account for database storage
     */
    public static final String LOG_ENCRYPTING_ACCOUNT_FOR_DB = "Encrypting account for database storage (length: {})";

    /**
     * Log message: Account encrypted successfully for database
     */
    public static final String LOG_ACCOUNT_ENCRYPTED_FOR_DB = "Account encrypted successfully for database";

    /**
     * Log message: Decrypting account from database
     */
    public static final String LOG_DECRYPTING_ACCOUNT_FROM_DB = "Decrypting account from database (cipherText length: {})";

    /**
     * Log message: Account decrypted successfully from database
     */
    public static final String LOG_ACCOUNT_DECRYPTED_FROM_DB = "Account decrypted successfully from database";

    /**
     * Log message: Encrypting data with RSA
     */
    public static final String LOG_ENCRYPTING_DATA_RSA = "Encrypting data with RSA (data length: {})";

    /**
     * Log message: RSA encryption completed successfully
     */
    public static final String LOG_RSA_ENCRYPTION_COMPLETED = "RSA encryption completed successfully";

    /**
     * Log message: Decrypting data with RSA
     */
    public static final String LOG_DECRYPTING_DATA_RSA = "Decrypting data with RSA (data length: {})";

    /**
     * Log message: RSA decryption completed successfully
     */
    public static final String LOG_RSA_DECRYPTION_COMPLETED = "RSA decryption completed successfully";

    /**
     * Log message: Parsing RSA public key from Base64
     */
    public static final String LOG_PARSING_RSA_PUBLIC_KEY = "Parsing RSA public key from Base64";

    /**
     * Log message: RSA public key parsed successfully
     */
    public static final String LOG_RSA_PUBLIC_KEY_PARSED = "RSA public key parsed successfully";

    /**
     * Log message: Parsing RSA private key from Base64
     */
    public static final String LOG_PARSING_RSA_PRIVATE_KEY = "Parsing RSA private key from Base64";

    /**
     * Log message: RSA private key parsed successfully
     */
    public static final String LOG_RSA_PRIVATE_KEY_PARSED = "RSA private key parsed successfully";
}
