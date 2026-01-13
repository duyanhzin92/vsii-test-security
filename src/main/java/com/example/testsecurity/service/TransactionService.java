package com.example.testsecurity.service;

/**
 * Service interface cho xử lý giao dịch banking.
 * <p>
 * <b>Business Logic:</b>
 * <ul>
 *     <li>Mỗi giao dịch chuyển khoản sẽ phát sinh 2 bản ghi:</li>
 *     <li>1. Bản ghi NỢ cho tài khoản nguồn (InDebt = amount, Have = 0)</li>
 *     <li>2. Bản ghi CÓ cho tài khoản đích (InDebt = 0, Have = amount)</li>
 * </ul>
 * <p>
 * <b>Encryption:</b>
 * <ul>
 *     <li>Account Number được mã hóa bằng AES trước khi lưu vào database</li>
 *     <li>Tất cả các field (TransactionID, Account, Amount, Time) được mã hóa RSA khi truyền giữa services</li>
 * </ul>
 */
public interface TransactionService {

    /**
     * Xử lý giao dịch chuyển khoản.
     * <p>
     * Flow:
     * <ol>
     *     <li>Validate input (transactionId, fromAccount, toAccount, amount, time)</li>
     *     <li>Check business rules (số dư, tài khoản tồn tại, ...)</li>
     *     <li>Tạo 2 bản ghi transaction history:</li>
     *     <li>   - Bản ghi NỢ cho tài khoản nguồn (InDebt = amount, Have = 0)</li>
     *     <li>   - Bản ghi CÓ cho tài khoản đích (InDebt = 0, Have = amount)</li>
     *     <li>AES encrypt Account Number trước khi lưu vào database</li>
     * </ol>
     * <p>
     * <b>Transaction Management:</b>
     * <ul>
     *     <li>Method này được đánh dấu @Transactional</li>
     *     <li>Nếu có lỗi, tất cả các bản ghi sẽ được rollback (atomicity)</li>
     * </ul>
     *
     * @param transactionId Mã giao dịch (đã được RSA decrypt)
     * @param fromAccount  Số tài khoản nguồn (đã được RSA decrypt)
     * @param toAccount    Số tài khoản đích (đã được RSA decrypt)
     * @param amount       Số tiền chuyển khoản (đã được RSA decrypt và convert sang BigDecimal)
     * @param time         Thời gian phát sinh giao dịch (đã được RSA decrypt và parse sang LocalDateTime)
     * @throws com.example.testsecurity.exception.BusinessException nếu có lỗi nghiệp vụ (số dư không đủ, tài khoản không tồn tại, ...)
     * @throws com.example.testsecurity.exception.CryptoException    nếu có lỗi mã hóa/giải mã
     */
    void processTransfer(String transactionId, String fromAccount, String toAccount, java.math.BigDecimal amount, java.time.LocalDateTime time);
}
