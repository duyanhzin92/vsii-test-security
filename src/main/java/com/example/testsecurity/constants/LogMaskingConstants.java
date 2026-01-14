package com.example.testsecurity.constants;

/**
 * Constants cho Log Masking module.
 * <p>
 * Chứa các constants liên quan đến masking sensitive data trong logs.
 */
public final class LogMaskingConstants {

    /**
     * Private constructor để ngăn instantiation
     */
    private LogMaskingConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    /**
     * Ký tự dùng để che thông tin nhạy cảm trong logs
     */
    public static final String MASK_CHAR = "?";

    /**
     * Default mask value khi giá trị null hoặc empty
     */
    public static final String DEFAULT_MASK_VALUE = "?";

    /**
     * Pattern regex để match transactionId trong log message
     */
    public static final String PATTERN_TRANSACTION_ID = "(?i)transactionId[=:]\\s*([^,\\s}]+)";

    /**
     * Pattern regex để match account trong log message
     */
    public static final String PATTERN_ACCOUNT = "(?i)account[=:]\\s*([^,\\s}]+)";

    /**
     * Pattern regex để match inDebt trong log message
     */
    public static final String PATTERN_IN_DEBT = "(?i)inDebt[=:]\\s*([^,\\s}]+)";

    /**
     * Pattern regex để match have trong log message
     */
    public static final String PATTERN_HAVE = "(?i)have[=:]\\s*([^,\\s}]+)";

    /**
     * Pattern regex để match time trong log message
     */
    public static final String PATTERN_TIME = "(?i)time[=:]\\s*([^,\\s}]+)";

    /**
     * Replacement string cho transactionId
     */
    public static final String REPLACEMENT_TRANSACTION_ID = "transactionId=?";

    /**
     * Replacement string cho account
     */
    public static final String REPLACEMENT_ACCOUNT = "account=?";

    /**
     * Replacement string cho inDebt
     */
    public static final String REPLACEMENT_IN_DEBT = "inDebt=?";

    /**
     * Replacement string cho have
     */
    public static final String REPLACEMENT_HAVE = "have=?";

    /**
     * Replacement string cho time
     */
    public static final String REPLACEMENT_TIME = "time=?";
}
