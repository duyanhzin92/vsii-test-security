# Banking Transaction System - Tài liệu Dự án

## 📋 Mục đích Dự án

Dự án **Banking Transaction System** là một hệ thống quản lý giao dịch chuyển khoản ngân hàng với các tính năng bảo mật cao cấp:

### Mục tiêu chính:
1. **Bảo mật dữ liệu nhạy cảm**: Mã hóa số tài khoản khi lưu vào database và mã hóa dữ liệu khi truyền giữa các services
2. **Tuân thủ chuẩn ngân hàng**: Áp dụng các best practices về bảo mật và quản lý giao dịch trong ngành ngân hàng
3. **Tính toàn vẹn dữ liệu**: Đảm bảo mỗi giao dịch chuyển khoản được ghi nhận đầy đủ và chính xác (2 bản ghi: NỢ và CÓ)
4. **Logging an toàn**: Che thông tin nhạy cảm trong logs để tránh rò rỉ dữ liệu

---

## 🏗️ Kiến trúc Hệ thống

### 1. **Encryption Strategy (Chiến lược Mã hóa)**

#### AES Encryption (Database Storage)
- **Mục đích**: Mã hóa Account Number khi lưu vào database
- **Algorithm**: AES-256/GCM (Galois/Counter Mode)
- **Đặc điểm**:
  - Symmetric encryption (nhanh, phù hợp cho dữ liệu lớn)
  - Cung cấp cả confidentiality và integrity (authentication tag)
  - Mỗi lần encrypt sử dụng IV ngẫu nhiên → cùng plaintext cho ra ciphertext khác nhau

#### RSA Encryption (Service Communication)
- **Mục đích**: Mã hóa dữ liệu khi truyền giữa services
- **Algorithm**: RSA-2048/ECB/PKCS1Padding
- **Đặc điểm**:
  - Asymmetric encryption (public key để encrypt, private key để decrypt)
  - Chỉ dùng cho dữ liệu nhỏ (≤ 245 bytes cho RSA-2048)
  - Dùng để mã hóa: TransactionID, Account, Amount, Time

### 2. **Database Schema**

#### Bảng `transaction_history`

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | BIGINT | Primary key, auto increment |
| `transaction_id` | VARCHAR(100) | Mã giao dịch (unique) |
| `account` | VARCHAR(500) | Số tài khoản (đã mã hóa AES) |
| `in_debt` | DECIMAL(19,2) | Số tiền nợ |
| `have` | DECIMAL(19,2) | Số tiền có |
| `time` | DATETIME | Thời gian phát sinh giao dịch |

**Business Logic:**
- Mỗi giao dịch chuyển khoản tạo **2 bản ghi**:
  - **Bản ghi NỢ** cho tài khoản nguồn: `InDebt = amount`, `Have = 0`
  - **Bản ghi CÓ** cho tài khoản đích: `InDebt = 0`, `Have = amount`

### 3. **API Endpoints**

#### POST `/api/transactions/transfer`
Xử lý giao dịch chuyển khoản.

**Request Body** (tất cả field đã được RSA encrypt):
```json
{
  "transactionId": "RSA_ENCRYPTED_TRANSACTION_ID",
  "fromAccount": "RSA_ENCRYPTED_FROM_ACCOUNT",
  "toAccount": "RSA_ENCRYPTED_TO_ACCOUNT",
  "amount": "RSA_ENCRYPTED_AMOUNT",
  "time": "RSA_ENCRYPTED_TIME"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Transfer transaction processed successfully",
  "data": {
    "transactionId": "TXN123456789",
    "status": "SUCCESS",
    "message": "Transfer transaction processed successfully"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

#### GET `/api/transactions/public-key`
Lấy RSA Public Key để client mã hóa dữ liệu.

**Response**:
```json
{
  "success": true,
  "message": "RSA Public Key retrieved successfully",
  "data": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 🔐 Encryption Flow (Luồng Mã hóa)

### Client → Server Flow:

```
1. Client gọi GET /api/transactions/public-key
   → Nhận RSA Public Key

2. Client RSA encrypt các field:
   - transactionId = RSA_encrypt("TXN123456789", publicKey)
   - fromAccount = RSA_encrypt("1234567890", publicKey)
   - toAccount = RSA_encrypt("9876543210", publicKey)
   - amount = RSA_encrypt("1000000.50", publicKey)
   - time = RSA_encrypt("2024-01-15T10:30:00", publicKey)

3. Client gửi POST /api/transactions/transfer với encrypted data

4. Server RSA decrypt các field với private key

5. Server validate và parse dữ liệu

6. Server xử lý nghiệp vụ:
   - Tạo bản ghi NỢ cho tài khoản nguồn
   - Tạo bản ghi CÓ cho tài khoản đích
   - AES encrypt Account trước khi lưu vào database

7. Server trả về response SUCCESS
```

---

## 🛠️ Hướng dẫn Chạy Dự án

### Prerequisites (Yêu cầu)

- **Java**: JDK 17 hoặc cao hơn
- **Maven**: 3.6+ 
- **MySQL**: 8.0+
- **IDE**: IntelliJ IDEA (khuyến nghị) hoặc Eclipse

### Bước 1: Cấu hình Database

1. Tạo database:
```sql
CREATE DATABASE bankdb;
```

2. Cấu hình trong `application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankdb
    username: root
    password: 123456  # Thay đổi theo cấu hình của bạn
```

3. Bảng sẽ được tự động tạo khi chạy ứng dụng (do `ddl-auto: update`)

### Bước 2: Cấu hình Encryption Keys

1. **AES Key** (bắt buộc):
   - Key hiện tại trong `application.yaml`: `MLEQ/ogfPk0z7ZtutxRWRodUqu48mEvorrUWagjq5Sc=`
   - Để generate key mới, chạy:
   ```java
   SecretKey key = AesUtil.generateKey();
   String keyBase64 = AesUtil.keyToString(key);
   System.out.println(keyBase64);
   ```

2. **RSA Keys** (optional):
   - Nếu không có trong config, hệ thống sẽ tự động generate temporary keys
   - Để generate keys mới, chạy:
   ```java
   KeyPair keyPair = RsaUtil.generateKeyPair();
   String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
   String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
   ```

### Bước 3: Build và Chạy Ứng dụng

#### Cách 1: Sử dụng Maven
```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

#### Cách 2: Sử dụng IDE
1. Mở project trong IntelliJ IDEA
2. Right-click vào `TestsecurityApplication.java`
3. Chọn `Run 'TestsecurityApplication'`

### Bước 4: Generate Sample Data

#### Cách 1: Sử dụng DataGenerator (Khuyến nghị)
1. Chạy ứng dụng với argument: `--generate-data`
2. Copy SQL statements từ console
3. Chạy SQL trong MySQL Workbench

#### Cách 2: Sử dụng API để tạo data
Sử dụng Postman hoặc Swagger UI để gọi API `/api/transactions/transfer`

### Bước 5: Truy cập Swagger UI

1. Mở trình duyệt
2. Truy cập: `http://localhost:8080/swagger-ui.html`
3. Xem và test các API endpoints

---

## 📝 Testing

### Test Case 1: Chuyển khoản thành công
1. Lấy RSA Public Key: `GET /api/transactions/public-key`
2. RSA encrypt các field:
   - transactionId: "TXN20240115001"
   - fromAccount: "1234567890"
   - toAccount: "9876543210"
   - amount: "1000000.00"
   - time: "2024-01-15T10:30:00"
3. Gửi POST `/api/transactions/transfer` với encrypted data
4. Kiểm tra response: `success = true`
5. Kiểm tra database: Có 2 bản ghi với cùng transactionId

### Test Case 2: Duplicate Transaction ID
1. Gửi cùng một transactionId 2 lần
2. Lần thứ 2 sẽ trả về lỗi: `DUPLICATE_TRANSACTION_ID`

### Test Case 3: Invalid Amount
1. Gửi amount = "0" hoặc amount = "-1000"
2. Trả về lỗi: `INVALID_AMOUNT`

### Test Case 4: Invalid Time Format
1. Gửi time với format sai (ví dụ: "2024-01-15")
2. Trả về lỗi: `VALIDATION_ERROR`

---

## 🔍 Kiểm tra Database

### Query tất cả giao dịch:
```sql
SELECT * FROM bankdb.transaction_history;
```

### Query giao dịch theo Transaction ID:
```sql
SELECT * FROM bankdb.transaction_history 
WHERE transaction_id = 'TXN20240115001';
```

### Lưu ý:
- Field `account` trong database đã được mã hóa AES
- Không thể query trực tiếp bằng Account number plaintext
- Để query, cần mã hóa Account number trước (sử dụng cùng AES key)

---

## 📁 Cấu trúc Project

```
testsecurity/
├── src/
│   ├── main/
│   │   ├── java/com/example/testsecurity/
│   │   │   ├── config/          # Configuration classes
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/      # REST Controllers
│   │   │   │   └── TransactionController.java
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   │   └── TransferRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java
│   │   │   │       ├── ErrorResponse.java
│   │   │   │       └── TransferResponse.java
│   │   │   ├── entity/          # JPA Entities
│   │   │   │   └── TransactionHistory.java
│   │   │   ├── exception/       # Exception classes
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── CryptoException.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── repository/      # JPA Repositories
│   │   │   │   └── TransactionHistoryRepository.java
│   │   │   ├── service/         # Service interfaces
│   │   │   │   ├── EncryptionService.java
│   │   │   │   └── TransactionService.java
│   │   │   ├── service/impl/   # Service implementations
│   │   │   │   └── TransactionServiceImpl.java
│   │   │   ├── util/            # Utility classes
│   │   │   │   ├── AesUtil.java
│   │   │   │   ├── DataGenerator.java
│   │   │   │   ├── LogMaskingUtil.java
│   │   │   │   └── RsaUtil.java
│   │   │   └── TestsecurityApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── data.sql
│   └── test/                    # Test classes
└── pom.xml
```

---

## ⚠️ Lưu ý Quan trọng

### Security:
1. **Không commit encryption keys vào Git**
2. **Sử dụng environment variables hoặc key management service trong production**
3. **Rotate keys định kỳ**
4. **Backup keys an toàn**

### Performance:
1. **RSA encryption chậm** → Chỉ dùng cho dữ liệu nhỏ
2. **AES encryption nhanh** → Dùng cho database storage
3. **Cân nhắc caching** cho các operations thường xuyên

### Production:
1. **Disable Swagger UI** trong production
2. **Sử dụng HTTPS** cho tất cả API calls
3. **Implement rate limiting**
4. **Monitor và log đầy đủ**

---

## 📚 Tài liệu Tham khảo

- [AES Encryption](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)
- [RSA Encryption](https://en.wikipedia.org/wiki/RSA_(cryptosystem))
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Swagger/OpenAPI](https://swagger.io/specification/)

---

## 👥 Liên hệ

Nếu có thắc mắc hoặc cần hỗ trợ, vui lòng liên hệ:
- Email: support@banking.com
- Documentation: Xem file `README.md` và `PROJECT_DOCUMENTATION.md`

---

**Version**: 1.0.0  
**Last Updated**: 2024-01-15
