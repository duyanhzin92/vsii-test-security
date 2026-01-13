package com.example.testsecurity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wrapper class cho tất cả API responses.
 * <p>
 * Chuẩn hóa format response để client dễ xử lý:
 * <ul>
 *     <li>success: true/false</li>
 *     <li>message: Thông báo tổng quan</li>
 *     <li>data: Dữ liệu thực tế (có thể null nếu có lỗi)</li>
 *     <li>timestamp: Thời gian server xử lý request</li>
 * </ul>
 *
 * @param <T> Type của data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Trạng thái thành công hay không
     */
    private Boolean success;

    /**
     * Thông báo tổng quan
     */
    private String message;

    /**
     * Dữ liệu thực tế (có thể null nếu có lỗi)
     */
    private T data;

    /**
     * Thời gian server xử lý request
     */
    private LocalDateTime timestamp;
}
