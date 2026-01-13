package com.example.testsecurity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class cho Swagger/OpenAPI documentation.
 * <p>
 * Cấu hình Swagger UI để hiển thị API documentation với đầy đủ thông tin về:
 * <ul>
 *     <li>API endpoints</li>
 *     <li>Request/Response models</li>
 *     <li>Error responses</li>
 *     <li>Authentication requirements (nếu có)</li>
 * </ul>
 * <p>
 * <b>Access Swagger UI:</b>
 * <ul>
 *     <li>URL: http://localhost:8080/swagger-ui.html</li>
 *     <li>API Docs JSON: http://localhost:8080/v3/api-docs</li>
 * </ul>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Cấu hình OpenAPI bean cho Swagger documentation
     *
     * @return OpenAPI object chứa thông tin về API
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Transaction System API")
                        .version("1.0.0")
                        .description("""
                                API để xử lý giao dịch chuyển khoản ngân hàng với mã hóa dữ liệu nhạy cảm.
                                
                                ## Tính năng bảo mật:
                                - **AES Encryption**: Mã hóa số tài khoản khi lưu vào database
                                - **RSA Encryption**: Mã hóa dữ liệu khi truyền giữa các services
                                - **Data Masking**: Che thông tin nhạy cảm trong logs
                                
                                ## Encryption Flow:
                                1. Client/Service: RSA encrypt TransactionID, FromAccount, ToAccount, Amount, Time với server's public key
                                2. Server: RSA decrypt các field này với private key
                                3. Server: Validate và parse dữ liệu
                                4. Server: Xử lý nghiệp vụ (tạo 2 bản ghi: nợ và có)
                                5. Server: AES encrypt Account trước khi lưu vào database
                                
                                ## Lưu ý:
                                - Tất cả các field trong TransferRequest phải được mã hóa RSA trước khi gửi
                                - Sử dụng endpoint `/api/transactions/public-key` để lấy RSA public key
                                - Format time: ISO-8601 (ví dụ: 2024-01-15T10:30:00)
                                """)
                        .contact(new Contact()
                                .name("Banking Transaction System")
                                .email("support@banking.com")
                                .url("https://banking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
