package com.example.testsecurity.service;

import com.example.testsecurity.constants.EncryptionConstants;
import com.example.testsecurity.exception.CryptoException;
import com.example.testsecurity.util.AesUtil;
import com.example.testsecurity.util.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Service để xử lý mã hóa AES và RSA cho banking transaction system.
 * <p>
 * <b>Encryption Strategy:</b>
 * <ul>
 *     <li><b>AES:</b> Mã hóa Account Number khi lưu vào database (symmetric encryption, nhanh)</li>
 *     <li><b>RSA:</b> Mã hóa TransactionID, Account, Amount, Time khi truyền giữa services (asymmetric encryption)</li>
 * </ul>
 * <p>
 * <b>Key Management:</b>
 * <ul>
 *     <li>AES key: Load từ application.yaml (encryption.aes.key)</li>
 *     <li>RSA keys: Load từ application.yaml (encryption.rsa.public-key, encryption.rsa.private-key)</li>
 *     <li>Nếu RSA keys không có trong config, sẽ generate temporary keys (chỉ dùng cho development)</li>
 * </ul>
 */
@Service
@Slf4j
public class EncryptionService {

    /**
     * AES key dùng để mã hóa Account Number khi lưu vào database
     */
    private final SecretKey aesKey;

    /**
     * RSA public key dùng để mã hóa dữ liệu (client dùng để encrypt trước khi gửi)
     */
    private final PublicKey rsaPublicKey;

    /**
     * RSA private key dùng để giải mã dữ liệu (server dùng để decrypt khi nhận)
     */
    private final PrivateKey rsaPrivateKey;

    /**
     * Constructor để khởi tạo EncryptionService với keys từ config
     *
     * @param aesKeyBase64        AES key dạng Base64 (bắt buộc)
     * @param rsaPublicKeyBase64  RSA public key dạng Base64 (optional, nếu không có sẽ generate tạm)
     * @param rsaPrivateKeyBase64 RSA private key dạng Base64 (optional, nếu không có sẽ generate tạm)
     */
    public EncryptionService(
            @Value("${encryption.aes.key}") String aesKeyBase64,
            @Value("${encryption.rsa.public-key:}") String rsaPublicKeyBase64,
            @Value("${encryption.rsa.private-key:}") String rsaPrivateKeyBase64) {

        // Load AES key (bắt buộc)
        try {
            this.aesKey = AesUtil.keyFromString(aesKeyBase64);
            log.info(EncryptionConstants.LOG_AES_KEY_LOADED);
        } catch (IllegalArgumentException e) {
            log.error("Failed to load AES key from config: invalid Base64 format", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_AES_KEY_FORMAT, e);
        }

        // Load RSA keys (optional - nếu không có thì generate tạm)
        PublicKey tempPublicKey;
        PrivateKey tempPrivateKey;

        if (rsaPublicKeyBase64 != null && !rsaPublicKeyBase64.isEmpty()
                && rsaPrivateKeyBase64 != null && !rsaPrivateKeyBase64.isEmpty()) {
            try {
                // Thử parse RSA keys từ config
                tempPublicKey = parseRSAPublicKey(rsaPublicKeyBase64);
                tempPrivateKey = parseRSAPrivateKey(rsaPrivateKeyBase64);
                log.info(EncryptionConstants.LOG_RSA_KEYS_LOADED);
            } catch (CryptoException e) {
                log.error("Failed to parse RSA keys from config, will generate temporary keys", e);
                // Fallback: generate temporary keys
                KeyPair tempKeyPair = generateTemporaryRSAKeys();
                tempPublicKey = tempKeyPair.getPublic();
                tempPrivateKey = tempKeyPair.getPrivate();
            }
        } else {
            // Generate temporary RSA keys
            log.warn(EncryptionConstants.LOG_RSA_KEYS_NOT_FOUND);
            KeyPair tempKeyPair = generateTemporaryRSAKeys();
            tempPublicKey = tempKeyPair.getPublic();
            tempPrivateKey = tempKeyPair.getPrivate();
        }

        this.rsaPublicKey = tempPublicKey;
        this.rsaPrivateKey = tempPrivateKey;

        log.info(EncryptionConstants.LOG_ENCRYPTION_SERVICE_INITIALIZED);
    }

    // ============ AES Methods (for database encryption) ============

    /**
     * Mã hóa Account Number bằng AES-256/GCM trước khi lưu vào database
     * <p>
     * Dùng cho: Account Number field trong TransactionHistory entity
     *
     * @param account Account Number (plaintext)
     * @return Account Number đã mã hóa (Base64 encoded)
     * @throws CryptoException nếu có lỗi trong quá trình mã hóa
     */
    public String encryptAccountForDatabase(String account) {
        try {
            log.debug(EncryptionConstants.LOG_ENCRYPTING_ACCOUNT_FOR_DB, account != null ? account.length() : 0);
            String encrypted = AesUtil.encrypt(account, aesKey);
            log.debug(EncryptionConstants.LOG_ACCOUNT_ENCRYPTED_FOR_DB);
            return encrypted;
        } catch (CryptoException e) {
            log.error("AES encryption failed for account: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("AES encryption failed: invalid account format", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_ACCOUNT_FORMAT_AES, e);
        }
    }

    /**
     * Giải mã Account Number từ database
     * <p>
     * Dùng khi cần đọc Account Number từ database (ví dụ: để hiển thị hoặc xử lý nghiệp vụ)
     *
     * @param encryptedAccount Account Number đã mã hóa (Base64 encoded)
     * @return Account Number gốc (plaintext)
     * @throws CryptoException nếu có lỗi trong quá trình giải mã (key sai, data bị tamper, ...)
     */
    public String decryptAccountFromDatabase(String encryptedAccount) {
        try {
            log.debug(EncryptionConstants.LOG_DECRYPTING_ACCOUNT_FROM_DB, encryptedAccount != null ? encryptedAccount.length() : 0);
            String decrypted = AesUtil.decrypt(encryptedAccount, aesKey);
            log.debug(EncryptionConstants.LOG_ACCOUNT_DECRYPTED_FROM_DB);
            return decrypted;
        } catch (CryptoException e) {
            log.error("AES decryption failed for account: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("AES decryption failed: invalid Base64 ciphertext", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_BASE64_CIPHERTEXT, e);
        }
    }

    // ============ RSA Methods (for service communication) ============

    /**
     * Mã hóa dữ liệu bằng RSA Public Key
     * <p>
     * Dùng để mã hóa TransactionID, Account, Amount, Time khi truyền giữa services
     * <p>
     * <b>Lưu ý:</b> RSA chỉ dùng cho dữ liệu nhỏ (≤ 245 bytes cho RSA-2048)
     *
     * @param data dữ liệu cần mã hóa (TransactionID, Account, Amount, Time)
     * @return chuỗi đã mã hóa (Base64 encoded)
     * @throws CryptoException nếu có lỗi (data quá lớn, key invalid, ...)
     */
    public String encryptRSA(String data) {
        try {
            log.debug(EncryptionConstants.LOG_ENCRYPTING_DATA_RSA, data != null ? data.length() : 0);
            String encrypted = RsaUtil.encrypt(data, rsaPublicKey);
            log.debug(EncryptionConstants.LOG_RSA_ENCRYPTION_COMPLETED);
            return encrypted;
        } catch (CryptoException e) {
            log.error("RSA encryption failed: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("RSA encryption failed: invalid input data", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_INPUT_DATA_RSA, e);
        }
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key
     * <p>
     * Dùng để giải mã TransactionID, Account, Amount, Time khi nhận từ client/service khác
     *
     * @param encryptedData chuỗi đã mã hóa (Base64 encoded)
     * @return dữ liệu gốc đã được giải mã
     * @throws CryptoException nếu có lỗi (key sai, data bị tamper, ...)
     */
    public String decryptRSA(String encryptedData) {
        try {
            log.debug(EncryptionConstants.LOG_DECRYPTING_DATA_RSA, encryptedData != null ? encryptedData.length() : 0);
            String decrypted = RsaUtil.decrypt(encryptedData, rsaPrivateKey);
            log.debug(EncryptionConstants.LOG_RSA_DECRYPTION_COMPLETED);
            return decrypted;
        } catch (CryptoException e) {
            log.error("RSA decryption failed: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("RSA decryption failed: invalid Base64 ciphertext", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_BASE64_CIPHERTEXT_RSA, e);
        }
    }

    /**
     * Get RSA Public Key dạng Base64 (để gửi cho client)
     * <p>
     * Client cần public key này để mã hóa TransactionID, Account, Amount, Time trước khi gửi lên server
     *
     * @return RSA Public Key Base64 string
     */
    public String getRSAPublicKeyBase64() {
        return java.util.Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded());
    }

    // ============ Helper Methods ============

    /**
     * Parse RSA Public Key từ Base64 string
     *
     * @param base64Key RSA public key dạng Base64
     * @return PublicKey object
     * @throws CryptoException nếu parse failed
     */
    private PublicKey parseRSAPublicKey(String base64Key) {
        try {
            log.debug(EncryptionConstants.LOG_PARSING_RSA_PUBLIC_KEY);
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance(EncryptionConstants.ALGORITHM_RSA);
            PublicKey publicKey = keyFactory.generatePublic(spec);
            log.debug(EncryptionConstants.LOG_RSA_PUBLIC_KEY_PARSED);
            return publicKey;
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse RSA public key: invalid Base64 format", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_BASE64_RSA_PUBLIC_KEY, e);
        } catch (InvalidKeySpecException e) {
            log.error("Failed to parse RSA public key: invalid key specification", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_RSA_PUBLIC_KEY_SPEC, e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to parse RSA public key: RSA algorithm not supported", e);
            throw new CryptoException(EncryptionConstants.ERR_RSA_ALGORITHM_NOT_SUPPORTED, e);
        }
    }

    /**
     * Parse RSA Private Key từ Base64 string
     *
     * @param base64Key RSA private key dạng Base64
     * @return PrivateKey object
     * @throws CryptoException nếu parse failed
     */
    private PrivateKey parseRSAPrivateKey(String base64Key) {
        try {
            log.debug(EncryptionConstants.LOG_PARSING_RSA_PRIVATE_KEY);
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance(EncryptionConstants.ALGORITHM_RSA);
            PrivateKey privateKey = keyFactory.generatePrivate(spec);
            log.debug(EncryptionConstants.LOG_RSA_PRIVATE_KEY_PARSED);
            return privateKey;
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse RSA private key: invalid Base64 format", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_BASE64_RSA_PRIVATE_KEY, e);
        } catch (InvalidKeySpecException e) {
            log.error("Failed to parse RSA private key: invalid key specification", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_RSA_PRIVATE_KEY_SPEC, e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to parse RSA private key: RSA algorithm not supported", e);
            throw new CryptoException(EncryptionConstants.ERR_RSA_ALGORITHM_NOT_SUPPORTED, e);
        }
    }

    /**
     * Generate temporary RSA key pair (dùng khi không có keys trong config)
     * <p>
     * ⚠️ Chỉ dùng cho development/testing, không dùng trong production!
     *
     * @return KeyPair chứa PublicKey và PrivateKey
     * @throws CryptoException nếu generate failed
     */
    private KeyPair generateTemporaryRSAKeys() {
        try {
            log.warn(EncryptionConstants.LOG_GENERATING_TEMP_RSA_KEYS);
            KeyPair keyPair = RsaUtil.generateKeyPair();
            log.warn(EncryptionConstants.LOG_TEMP_RSA_KEYS_GENERATED);
            return keyPair;
        } catch (CryptoException e) {
            log.error("Failed to generate temporary RSA keys: {}", e.getMessage(), e);
            throw new CryptoException(EncryptionConstants.ERR_FAILED_TO_GENERATE_TEMP_RSA_KEYS, e);
        }
    }
}
