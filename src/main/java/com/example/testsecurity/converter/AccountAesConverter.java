package com.example.testsecurity.converter;

import com.example.testsecurity.constants.EncryptionConstants;
import com.example.testsecurity.exception.CryptoException;
import com.example.testsecurity.util.AesUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JPA AttributeConverter cho field account trong TransactionHistory.
 * <p>
 * - convertToDatabaseColumn: plaintext account -> AES/GCM -> Base64 (lưu xuống DB)
 * - convertToEntityAttribute: Base64 -> AES/GCM decrypt -> plaintext account
 */
@Component
@Converter(autoApply = false)
@Slf4j
public class AccountAesConverter implements AttributeConverter<String, String> {

    /**
     * AES key dùng để mã hóa/giải mã account.
     * <p>
     * Được load từ application.yml: encryption.aes.key
     */
    private static SecretKey aesKey;

    @Value("${encryption.aes.key}")
    public void setAesKey(String aesKeyBase64) {
        try {
            AccountAesConverter.aesKey = AesUtil.keyFromString(aesKeyBase64);
            log.info(EncryptionConstants.LOG_AES_KEY_LOADED);
        } catch (IllegalArgumentException e) {
            log.error("Failed to load AES key for AccountAesConverter", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_AES_KEY_FORMAT, e);
        }
    }

    /**
     * Mã hóa account trước khi lưu xuống database.
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.trim().isEmpty()) {
            return null;
        }
        try {
            log.debug("Encrypting account in AccountAesConverter (length: {})", attribute.length());
            return AesUtil.encrypt(attribute, aesKey);
        } catch (CryptoException e) {
            log.error("AES encryption failed in AccountAesConverter: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("AES encryption failed in AccountAesConverter: invalid account format", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_INPUT_DATA_AES, e);
        }
    }

    /**
     * Giải mã account sau khi đọc từ database.
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            log.debug("Decrypting account in AccountAesConverter (cipherText length: {})", dbData.length());
            return AesUtil.decrypt(dbData, aesKey);
        } catch (CryptoException e) {
            log.error("AES decryption failed in AccountAesConverter: {}", e.getMessage(), e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("AES decryption failed in AccountAesConverter: invalid Base64 ciphertext", e);
            throw new CryptoException(EncryptionConstants.ERR_INVALID_BASE64_CIPHERTEXT, e);
        }
    }
}

