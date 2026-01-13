package com.example.testsecurity.util;

import com.example.testsecurity.exception.CryptoException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class cho AES/GCM (Symmetric Encryption)
 * <p>
 * Sử dụng AES-256/GCM với các đặc điểm:
 * <ul>
 *     <li><b>Confidentiality:</b> Dữ liệu được mã hóa, không thể đọc được nếu không có key</li>
 *     <li><b>Integrity:</b> GCM mode cung cấp authentication tag, phát hiện nếu dữ liệu bị tamper</li>
 *     <li><b>IV (Initialization Vector):</b> Mỗi lần encrypt sử dụng IV ngẫu nhiên, đảm bảo cùng plaintext cho ra ciphertext khác nhau</li>
 * </ul>
 * <p>
 * <b>Lưu ý quan trọng:</b>
 * <ul>
 *     <li>AES dùng để mã hóa dữ liệu thật (Account Number khi lưu vào database)</li>
 *     <li>KHÔNG dùng RSA để mã hóa dữ liệu lớn (chỉ dùng cho key exchange)</li>
 *     <li>Key phải được bảo vệ cẩn thận (không commit vào git, dùng key management service trong production)</li>
 * </ul>
 */
public final class AesUtil {

    /**
     * Algorithm: AES với GCM mode và NoPadding
     * <p>
     * GCM (Galois/Counter Mode) là authenticated encryption mode:
     * - Cung cấp cả confidentiality và integrity
     * - Nhanh hơn CBC mode
     * - Không cần padding (NoPadding)
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * Key size: 256-bit (AES-256)
     * <p>
     * AES-256 là mức bảo mật cao nhất hiện tại, phù hợp cho banking system
     */
    private static final int KEY_SIZE = 256;

    /**
     * GCM IV length: 12 bytes (96-bit)
     * <p>
     * IV (Initialization Vector) phải là unique cho mỗi lần encrypt
     * 96-bit là kích thước được khuyến nghị bởi NIST
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * GCM authentication tag length: 128-bit
     * <p>
     * Tag này dùng để verify integrity của dữ liệu khi decrypt
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Private constructor để ngăn instantiation
     * <p>
     * Class này là utility class, chỉ chứa static methods
     */
    private AesUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encrypt plaintext bằng AES-256/GCM
     * <p>
     * Flow:
     * <ol>
     *     <li>Generate random IV (12 bytes)</li>
     *     <li>Initialize Cipher với ENCRYPT_MODE, key và IV</li>
     *     <li>Encrypt plaintext → ciphertext</li>
     *     <li>Combine IV + ciphertext → Base64 encode</li>
     * </ol>
     * <p>
     * Output format: Base64(IV (12 bytes) + Ciphertext + Auth Tag (16 bytes))
     *
     * @param plainText văn bản gốc cần mã hóa
     * @param key       AES SecretKey (256-bit)
     * @return chuỗi đã mã hóa (Base64 encoded, format: IV + Ciphertext + Tag)
     * @throws CryptoException nếu có lỗi trong quá trình mã hóa
     */
    public static String encrypt(String plainText, SecretKey key) {
        try {
            // Generate random IV cho mỗi lần encrypt
            // SecureRandom.getInstanceStrong() đảm bảo cryptographically secure random
            byte[] iv = SecureRandom.getInstanceStrong()
                    .generateSeed(GCM_IV_LENGTH);

            // Initialize Cipher với AES/GCM/NoPadding
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            // Encrypt plaintext
            byte[] cipherText = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)
            );

            // Combine IV + ciphertext để lưu trữ
            // Format: [IV (12 bytes)][Ciphertext + Auth Tag (variable length)]
            byte[] combined = ByteBuffer
                    .allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();

            // Base64 encode để lưu vào database (text format)
            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            // JVM không support AES/GCM algorithm (rất hiếm)
            throw new CryptoException("AES/GCM algorithm not supported by JVM", e);

        } catch (GeneralSecurityException e) {
            // Các lỗi khác: invalid key, cipher initialization failed, ...
            throw new CryptoException("AES/GCM encryption failed", e);
        }
    }

    /**
     * Decrypt AES/GCM ciphertext
     * <p>
     * Flow:
     * <ol>
     *     <li>Base64 decode → combined bytes</li>
     *     <li>Extract IV (12 bytes đầu) và ciphertext (phần còn lại)</li>
     *     <li>Initialize Cipher với DECRYPT_MODE, key và IV</li>
     *     <li>Decrypt ciphertext → plaintext</li>
     *     <li>GCM tự động verify authentication tag (nếu tag không khớp → throw AEADBadTagException)</li>
     * </ol>
     *
     * @param cipherText chuỗi đã mã hóa (Base64 encoded, format: IV + Ciphertext + Tag)
     * @param key        AES SecretKey (256-bit)
     * @return văn bản gốc đã được giải mã
     * @throws CryptoException nếu có lỗi trong quá trình giải mã (key sai, data bị tamper, format sai, ...)
     */
    public static String decrypt(String cipherText, SecretKey key) {
        try {
            // Base64 decode
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // Validate minimum length (phải có ít nhất IV + 1 byte ciphertext)
            if (decoded.length < GCM_IV_LENGTH + 1) {
                throw new CryptoException("Ciphertext too short (invalid format)");
            }

            // Extract IV và ciphertext
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            // Initialize Cipher với DECRYPT_MODE
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            // Decrypt (GCM tự động verify authentication tag)
            byte[] plainBytes = cipher.doFinal(encrypted);
            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            // Base64 decode failed
            throw new CryptoException("Invalid Base64 ciphertext format", e);

        } catch (AEADBadTagException e) {
            // Authentication tag không khớp → data bị tamper hoặc key sai
            throw new CryptoException("Invalid AES key or tampered ciphertext (authentication failed)", e);

        } catch (GeneralSecurityException e) {
            // Các lỗi khác: invalid key, cipher initialization failed, ...
            throw new CryptoException("AES/GCM decryption failed", e);
        }
    }

    /**
     * Generate AES 256-bit key
     * <p>
     * Dùng để generate key mới (ví dụ: khi setup hệ thống lần đầu)
     * <p>
     * <b>Lưu ý:</b> Key này phải được lưu trữ an toàn (không commit vào git)
     *
     * @return SecretKey (256-bit)
     * @throws CryptoException nếu generate failed
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(KEY_SIZE);
            return generator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            // JVM không support AES (rất hiếm)
            throw new CryptoException("Generate AES key failed: AES algorithm not supported", e);
        }
    }

    /**
     * Convert Base64 string → AES SecretKey
     * <p>
     * Dùng để load key từ config (application.yaml)
     *
     * @param keyString AES key dạng Base64 string
     * @return SecretKey object
     * @throws IllegalArgumentException nếu Base64 format không hợp lệ
     */
    public static SecretKey keyFromString(String keyString) {
        byte[] decoded = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decoded, "AES");
    }

    /**
     * Convert AES SecretKey → Base64 string
     * <p>
     * Dùng để serialize key để lưu vào config
     *
     * @param key SecretKey object
     * @return Base64 string
     */
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
