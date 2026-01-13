package com.example.testsecurity.util;

/**
 * Utility class để che (mask) thông tin nhạy cảm trong logs.
 * <p>
 * Trong banking system, các thông tin sau phải được che khi log:
 * <ul>
 *     <li>TransactionID: Mã giao dịch</li>
 *     <li>Account: Số tài khoản</li>
 *     <li>InDebt: Số tiền nợ</li>
 *     <li>Have: Số tiền có</li>
 *     <li>Time: Thời gian phát sinh giao dịch</li>
 * </ul>
 * <p>
 * <b>Lưu ý:</b>
 * <ul>
 *     <li>Chỉ che thông tin trong logs, KHÔNG che trong database</li>
 *     <li>Database vẫn lưu dữ liệu đã mã hóa (AES cho Account, plaintext cho các field khác)</li>
 *     <li>Logs chỉ dùng để debug, không được expose thông tin nhạy cảm</li>
 * </ul>
 */
public final class LogMaskingUtil {

    /**
     * Ký tự dùng để che thông tin nhạy cảm
     */
    private static final String MASK_CHAR = "?";

    /**
     * Private constructor để ngăn instantiation
     */
    private LogMaskingUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Che TransactionID trong log
     * <p>
     * Ví dụ: "TXN123456789" → "???????????"
     *
     * @param transactionId TransactionID cần che
     * @return chuỗi đã được che (toàn bộ ký tự thay bằng "?")
     */
    public static String maskTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isEmpty()) {
            return "?";
        }
        return MASK_CHAR.repeat(transactionId.length());
    }

    /**
     * Che Account Number trong log
     * <p>
     * Ví dụ: "1234567890" → "??????????"
     * <p>
     * <b>Lưu ý:</b> Account Number đã được mã hóa AES trong database,
     * nhưng khi log exception, có thể có plaintext Account → phải che
     *
     * @param account Account Number cần che
     * @return chuỗi đã được che (toàn bộ ký tự thay bằng "?")
     */
    public static String maskAccount(String account) {
        if (account == null || account.isEmpty()) {
            return "?";
        }
        return MASK_CHAR.repeat(account.length());
    }

    /**
     * Che số tiền (InDebt hoặc Have) trong log
     * <p>
     * Ví dụ: "1000000.50" → "??????????"
     *
     * @param amount số tiền cần che (có thể là String hoặc BigDecimal.toString())
     * @return chuỗi đã được che (toàn bộ ký tự thay bằng "?")
     */
    public static String maskAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return "?";
        }
        return MASK_CHAR.repeat(amount.length());
    }

    /**
     * Che thời gian (Time) trong log
     * <p>
     * Ví dụ: "2024-01-15T10:30:00" → "????????????????"
     *
     * @param time thời gian cần che (có thể là String hoặc LocalDateTime.toString())
     * @return chuỗi đã được che (toàn bộ ký tự thay bằng "?")
     */
    public static String maskTime(String time) {
        if (time == null || time.isEmpty()) {
            return "?";
        }
        return MASK_CHAR.repeat(time.length());
    }

    /**
     * Che tất cả thông tin nhạy cảm trong một message log
     * <p>
     * Tìm và thay thế các pattern phổ biến:
     * <ul>
     *     <li>transactionId=...</li>
     *     <li>account=...</li>
     *     <li>inDebt=...</li>
     *     <li>have=...</li>
     *     <li>time=...</li>
     * </ul>
     *
     * @param message message log cần che
     * @return message đã được che
     */
    public static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;

        // Pattern: transactionId=value hoặc transactionId:value
        masked = masked.replaceAll("(?i)transactionId[=:]\\s*([^,\\s}]+)", "transactionId=?");

        // Pattern: account=value hoặc account:value
        masked = masked.replaceAll("(?i)account[=:]\\s*([^,\\s}]+)", "account=?");

        // Pattern: inDebt=value hoặc inDebt:value
        masked = masked.replaceAll("(?i)inDebt[=:]\\s*([^,\\s}]+)", "inDebt=?");

        // Pattern: have=value hoặc have:value
        masked = masked.replaceAll("(?i)have[=:]\\s*([^,\\s}]+)", "have=?");

        // Pattern: time=value hoặc time:value
        masked = masked.replaceAll("(?i)time[=:]\\s*([^,\\s}]+)", "time=?");

        return masked;
    }
}
