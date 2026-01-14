# Banking Transaction System

Hệ thống quản lý giao dịch ngân hàng với mã hóa dữ liệu nhạy cảm.

## Tổng quan

Hệ thống này xử lý các giao dịch chuyển khoản với các tính năng bảo mật:
- **AES Encryption**: Mã hóa số tài khoản khi lưu vào database
- **RSA Encryption**: Mã hóa dữ liệu khi truyền giữa các services
- **Data Masking**: Che thông tin nhạy cảm trong logs

## Cấu trúc Database

### Bảng `transaction_history`

| Field | Type | Mô tả |
|-------|------|-------|
| id | BIGINT | Primary key, auto increment |
| transactionId | VARCHAR(100) | Mã giao dịch (unique) |
| account | VARCHAR(500) | Số tài khoản (đã mã hóa AES) |
| inDebt | DECIMAL(19,2) | Số tiền nợ |
| have | DECIMAL(19,2) | Số tiền có |
| time | DATETIME | Thời gian phát sinh giao dịch |

**Lưu ý:**
- Mỗi giao dịch chuyển khoản sẽ phát sinh 2 bản ghi:
  - 1 bản ghi NỢ cho tài khoản nguồn (InDebt = amount, Have = 0)
  - 1 bản ghi CÓ cho tài khoản đích (InDebt = 0, Have = amount)

## Encryption Strategy

### AES Encryption (Database Storage)
- **Mục đích**: Mã hóa Account Number khi lưu vào database
- **Algorithm**: AES-256/GCM
- **Key**: Load từ `application.yaml` (`encryption.aes.key`)
- **Format**: Base64(IV + Encrypted Data + Auth Tag)

### RSA Encryption (Service Communication)
- **Mục đích**: Mã hóa dữ liệu khi truyền giữa services
- **Algorithm**: RSA-2048/ECB/PKCS1Padding
- **Keys**: Load từ `application.yaml` (`encryption.rsa.public-key`, `encryption.rsa.private-key`)
- **Fields được mã hóa**: TransactionID, Account, Amount, Time

## API Endpoints

### 1. Chuyển khoản
```
POST /api/transactions/transfer
```

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

### 2. Lấy RSA Public Key
```
GET /api/transactions/public-key
```

**Response**:
```json
{
  "success": true,
  "message": "RSA Public Key retrieved successfully",
  "data": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Cấu trúc Project

```
src/main/java/com/example/testsecurity/
├── config/              # Configuration classes
├── controller/          # REST Controllers
│   └── TransactionController.java
├── dto/                 # Data Transfer Objects
│   ├── request/
│   │   └── TransferRequest.java
│   └── response/
│       ├── ApiResponse.java
│       ├── ErrorResponse.java
│       └── TransferResponse.java
├── entity/              # JPA Entities
│   └── TransactionHistory.java
├── exception/           # Exception classes
│   ├── BusinessException.java
│   ├── CryptoException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
├── repository/         # JPA Repositories
│   └── TransactionHistoryRepository.java
├── service/             # Service interfaces
│   ├── EncryptionService.java
│   └── TransactionService.java
├── service/impl/       # Service implementations
│   └── TransactionServiceImpl.java
├── util/               # Utility classes
│   ├── AesUtil.java
│   ├── LogMaskingUtil.java
│   └── RsaUtil.java
└── TestsecurityApplication.java
```

## Configuration

### application.yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankdb
    username: root
    password: 123456

encryption:
  aes:
    key: YOUR_AES_KEY_BASE64
  rsa:
    public-key: YOUR_RSA_PUBLIC_KEY_BASE64
    private-key: YOUR_RSA_PRIVATE_KEY_BASE64
```

## Security Features

### 1. Data Encryption
- Account Number được mã hóa AES trước khi lưu vào database
- Tất cả dữ liệu được mã hóa RSA khi truyền giữa services

### 2. Data Masking in Logs
- TransactionID, Account, Amount, Time được che bằng dấu "?" trong logs
- Đảm bảo thông tin nhạy cảm không bị expose trong log files

### 3. Error Handling
- Tất cả exceptions được log với masked data
- Response được chuẩn hóa theo format ApiResponse
- Không expose chi tiết kỹ thuật cho client

## Code Conventions

- **Service Layer**: Interface + Implementation pattern
- **Exception Handling**: Custom exceptions với ErrorCode enum
- **Logging**: Sử dụng SLF4J với masked data
- **Validation**: Jakarta Validation (@Valid, @NotBlank, ...)
- **Comments**: Đầy đủ JavaDoc cho tất cả classes và methods

## Development

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

## Generate Sample Data

### Cách 1: Sử dụng SampleDataGenerator (Khuyến nghị)

1. Chạy ứng dụng với argument `--generate-data`:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--generate-data"
   ```

2. Copy SQL statements từ console (các Account numbers đã được mã hóa AES)

3. Chạy SQL trong MySQL Workbench

### Cách 2: Sử dụng API để tạo data

Sử dụng Postman hoặc Swagger UI để gọi API `/api/transactions/transfer`

## Hướng dẫn Chạy Dự án

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

### Bước 2: Build và Chạy

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

### Bước 3: Generate Sample Data

```bash
# Chạy với flag --generate-data
mvn spring-boot:run -Dspring-boot.run.arguments="--generate-data"
```

### Bước 4: Truy cập Swagger UI

Mở trình duyệt: `http://localhost:8080/swagger-ui.html`

## Notes

- ⚠️ **Production**: Cần generate keys mới và lưu trữ an toàn (không commit vào git)
- ⚠️ **Key Management**: Nên sử dụng key management service (AWS KMS, HashiCorp Vault, ...) trong production
- ⚠️ **Database**: Account field trong database đã được mã hóa, không thể query trực tiếp
- ⚠️ **Sample Data**: Account numbers trong sample data đã được mã hóa AES, mỗi lần generate sẽ khác nhau (do IV random)