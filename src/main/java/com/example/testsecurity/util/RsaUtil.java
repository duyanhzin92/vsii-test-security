package com.example.testsecurity.util;

import com.example.testsecurity.exception.CryptoException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Utility class cho RSA (Asymmetric Encryption)
 * <p>
 * <b>⚠️ QUAN TRỌNG: RSA KHÔNG dùng để mã hóa dữ liệu lớn!</b>
 * <ul>
 *     <li>RSA chỉ dùng để mã hóa key nhỏ (như AES key) hoặc trao đổi key</li>
 *     <li>Giới hạn kích thước dữ liệu cho RSA-2048: ~245 bytes</li>
 *     <li>Nếu dữ liệu lớn hơn, phải dùng hybrid encryption (RSA encrypt AES key, AES encrypt data)</li>
 * </ul>
 * <p>
 * Trong banking system:
 * <ul>
 *     <li><b>RSA:</b> Mã hóa TransactionID, Account, Amount, Time khi truyền giữa services (vì các field này nhỏ)</li>
 *     <li><b>AES:</b> Mã hóa Account khi lưu vào database (vì có thể có nhiều records)</li>
 * </ul>
 */
public final class RsaUtil {

    /**
     * Algorithm: RSA với ECB mode và PKCS1Padding
     * <p>
     * - ECB mode: Mỗi block được encrypt độc lập (phù hợp cho RSA vì RSA encrypt từng block)
     * - PKCS1Padding: Padding scheme chuẩn cho RSA
     */
    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * Key size: 2048-bit
     * <p>
     * RSA-2048 là mức tối thiểu được khuyến nghị hiện tại (NIST, 2020)
     * RSA-1024 đã bị coi là không an toàn
     */
    private static final int KEY_SIZE = 2048;

    /**
     * Private constructor để ngăn instantiation
     */
    private RsaUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encrypt dữ liệu nhỏ bằng RSA Public Key
     * <p>
     * <b>Lưu ý:</b> Chỉ dùng cho dữ liệu nhỏ (≤ 245 bytes cho RSA-2048)
     * <p>
     * Flow:
     * <ol>
     *     <li>Convert plaintext → bytes (UTF-8)</li>
     *     <li>RSA encrypt với public key</li>
     *     <li>Base64 encode → string</li>
     * </ol>
     *
     * @param data      dữ liệu cần mã hóa (phải ≤ 245 bytes cho RSA-2048)
     * @param publicKey RSA Public Key
     * @return chuỗi đã mã hóa (Base64 encoded)
     * @throws CryptoException nếu có lỗi (data quá lớn, key invalid, ...)
     */
    public static String encrypt(String data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encrypted = cipher.doFinal(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (InvalidKeyException e) {
            throw new CryptoException("Invalid RSA public key", e);

        } catch (IllegalBlockSizeException e) {
            // Data quá lớn cho RSA encryption
            throw new CryptoException("Data too large for RSA encryption (max ~245 bytes for RSA-2048)", e);

        } catch (GeneralSecurityException e) {
            throw new CryptoException("RSA encryption failed", e);
        }
    }

    /**
     * Decrypt dữ liệu bằng RSA Private Key
     * <p>
     * Flow:
     * <ol>
     *     <li>Base64 decode → encrypted bytes</li>
     *     <li>RSA decrypt với private key</li>
     *     <li>Convert bytes → string (UTF-8)</li>
     * </ol>
     *
     * @param data       chuỗi đã mã hóa (Base64 encoded)
     * @param privateKey RSA Private Key
     * @return dữ liệu gốc đã được giải mã
     * @throws CryptoException nếu có lỗi (key sai, data bị corrupt, format sai, ...)
     */
    public static String decrypt(String data, PrivateKey privateKey) {
        try {
            // Base64 decode
            byte[] encrypted = Base64.getDecoder().decode(data);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            // Base64 decode failed
            throw new CryptoException("Invalid Base64 ciphertext", e);

        } catch (BadPaddingException e) {
            // Key sai hoặc data bị corrupt
            throw new CryptoException("Invalid RSA private key or corrupted ciphertext", e);

        } catch (GeneralSecurityException e) {
            throw new CryptoException("RSA decryption failed", e);
        }
    }

    /**
     * Generate RSA KeyPair (2048-bit)
     * <p>
     * Dùng để generate key pair mới (ví dụ: khi setup hệ thống lần đầu)
     * <p>
     * <b>Lưu ý:</b>
     * <ul>
     *     <li>Private key phải được bảo vệ cẩn thận (không commit vào git)</li>
     *     <li>Public key có thể share với client/services khác</li>
     *     <li>Trong production: nên dùng HSM (Hardware Security Module) hoặc key management service</li>
     * </ul>
     *
     * @return KeyPair chứa PublicKey và PrivateKey
     * @throws CryptoException nếu generate failed
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            return generator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            // JVM không support RSA (rất hiếm)
            throw new CryptoException("Generate RSA key pair failed: RSA algorithm not supported", e);
        }
    }
}
