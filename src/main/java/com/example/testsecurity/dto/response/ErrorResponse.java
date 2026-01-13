package com.example.testsecurity.dto.response;

import com.example.testsecurity.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho error response.
 * <p>
 * Chứa thông tin chi tiết về lỗi để client xử lý:
 * <ul>
 *     <li>errorCode: Mã lỗi nghiệp vụ (enum ErrorCode)</li>
 *     <li>message: Thông báo lỗi</li>
 *     <li>details: Chi tiết lỗi (optional, ví dụ: danh sách validation errors)</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Mã lỗi nghiệp vụ
     */
    private ErrorCode errorCode;

    /**
     * Thông báo lỗi
     */
    private String message;

    /**
     * Chi tiết lỗi (optional)
     * <p>
     * Ví dụ: Danh sách validation errors
     */
    private List<String> details;
}
