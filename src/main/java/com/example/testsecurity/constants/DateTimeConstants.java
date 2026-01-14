package com.example.testsecurity.constants;

import java.time.format.DateTimeFormatter;

/**
 * Constants cho DateTime formatting.
 * <p>
 * Chứa các DateTimeFormatter và format patterns được sử dụng trong dự án.
 */
public final class DateTimeConstants {

    /**
     * Private constructor để ngăn instantiation
     */
    private DateTimeConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    /**
     * DateTimeFormatter cho ISO-8601 format (LocalDateTime)
     * <p>
     * Format: yyyy-MM-ddTHH:mm:ss
     * <p>
     * Ví dụ: 2024-01-15T10:30:00
     */
    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * DateTimeFormatter cho SQL DATETIME format
     * <p>
     * Format: yyyy-MM-dd HH:mm:ss
     * <p>
     * Ví dụ: 2024-01-15 10:30:00
     */
    public static final DateTimeFormatter SQL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Pattern string cho ISO-8601 format
     */
    public static final String ISO_8601_PATTERN = "yyyy-MM-ddTHH:mm:ss";

    /**
     * Pattern string cho SQL DATETIME format
     */
    public static final String SQL_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Example ISO-8601 format string (dùng trong error messages)
     */
    public static final String ISO_8601_EXAMPLE = "2024-01-15T10:30:00";
}
