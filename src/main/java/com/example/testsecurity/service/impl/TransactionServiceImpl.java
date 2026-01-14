package com.example.testsecurity.service.impl;

import com.example.testsecurity.constants.LogMaskingConstants;
import com.example.testsecurity.constants.TransactionConstants;
import com.example.testsecurity.entity.TransactionHistory;
import com.example.testsecurity.exception.BusinessException;
import com.example.testsecurity.exception.CryptoException;
import com.example.testsecurity.exception.ErrorCode;
import com.example.testsecurity.repository.TransactionHistoryRepository;
import com.example.testsecurity.service.EncryptionService;
import com.example.testsecurity.service.TransactionService;
import com.example.testsecurity.util.LogMaskingUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation của TransactionService.
 * <p>
 * Xử lý logic nghiệp vụ cho banking transaction system với đầy đủ:
 * <ul>
 *     <li>Encryption/Decryption (AES cho database, RSA cho service communication)</li>
 *     <li>Business validation (số dư, tài khoản, ...)</li>
 *     <li>Transaction management (atomicity)</li>
 *     <li>Error handling với data masking trong logs</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    /**
     * Repository để lưu transaction history vào database
     */
    private final TransactionHistoryRepository repository;

    /**
     * Service để mã hóa/giải mã dữ liệu
     */
    private final EncryptionService encryptionService;

    /**
     * Xử lý giao dịch chuyển khoản.
     * <p>
     * Flow chi tiết:
     * <ol>
     *     <li>Validate input (transactionId, fromAccount, toAccount, amount, time)</li>
     *     <li>Check duplicate transactionId (idempotency)</li>
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
     *     <li>Nếu có lỗi ở bất kỳ bước nào, tất cả các bản ghi sẽ được rollback (atomicity)</li>
     *     <li>Đảm bảo tính nhất quán dữ liệu (consistency)</li>
     * </ul>
     *
     * @param transactionId Mã giao dịch (đã được RSA decrypt)
     * @param fromAccount   Số tài khoản nguồn (đã được RSA decrypt)
     * @param toAccount     Số tài khoản đích (đã được RSA decrypt)
     * @param amount        Số tiền chuyển khoản (đã được RSA decrypt và convert sang BigDecimal)
     * @param time          Thời gian phát sinh giao dịch (đã được RSA decrypt và parse sang LocalDateTime)
     * @throws BusinessException nếu có lỗi nghiệp vụ (số dư không đủ, tài khoản không tồn tại, duplicate transactionId, ...)
     * @throws CryptoException   nếu có lỗi mã hóa/giải mã
     */
    @Override
    @Transactional
    public void processTransfer(String transactionId, String fromAccount, String toAccount, BigDecimal amount, LocalDateTime time) {
        try {
            // Log với masked data (che thông tin nhạy cảm)
            log.info(TransactionConstants.LOG_PROCESSING_TRANSFER,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(fromAccount),
                    LogMaskingUtil.maskAccount(toAccount),
                    LogMaskingUtil.maskAmount(amount != null ? amount.toString() : LogMaskingConstants.DEFAULT_MASK_VALUE),
                    LogMaskingUtil.maskTime(time != null ? time.toString() : LogMaskingConstants.DEFAULT_MASK_VALUE));

            // Step 1: Check duplicate transactionId (idempotency)
            checkDuplicateTransactionId(transactionId);

            // Step 2: Check business rules (số dư, tài khoản tồn tại, ...)
            validateBusinessRules(fromAccount, toAccount, amount);

            // Step 3: Create 2 transaction records (NỢ và CÓ)
            createDebitRecord(transactionId, fromAccount, amount, time);
            createCreditRecord(transactionId, toAccount, amount, time);

            log.info(TransactionConstants.LOG_TRANSFER_PROCESSED_SUCCESS,
                    LogMaskingUtil.maskTransactionId(transactionId));

        } catch (BusinessException | CryptoException e) {
            // Re-throw business/crypto exceptions (đã được log với masked data ở các method con)
            throw e;

        } catch (Exception e) {
            // Log exception với masked data
            log.error(TransactionConstants.LOG_UNEXPECTED_ERROR_PROCESSING,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(fromAccount),
                    LogMaskingUtil.maskAccount(toAccount),
                    LogMaskingUtil.maskAmount(amount != null ? amount.toString() : LogMaskingConstants.DEFAULT_MASK_VALUE),
                    LogMaskingUtil.maskTime(time != null ? time.toString() : LogMaskingConstants.DEFAULT_MASK_VALUE),
                    e);

            // Wrap và throw BusinessException
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    TransactionConstants.ERR_UNEXPECTED_ERROR_PROCESSING_TRANSFER,
                    e
            );
        }
    }

    /**
     * Check duplicate transactionId (idempotency)
     * <p>
     * Đảm bảo mỗi transactionId chỉ được xử lý một lần (tránh duplicate transaction)
     *
     * @param transactionId Mã giao dịch
     * @throws BusinessException nếu transactionId đã tồn tại
     */
    private void checkDuplicateTransactionId(String transactionId) {
        boolean exists = repository.existsByTransactionId(transactionId);
        if (exists) {
            log.warn(TransactionConstants.LOG_DUPLICATE_TRANSACTION_ID,
                    LogMaskingUtil.maskTransactionId(transactionId));
            throw new BusinessException(
                    ErrorCode.DUPLICATE_TRANSACTION_ID,
                    TransactionConstants.ERR_TRANSACTION_ID_ALREADY_EXISTS
            );
        }
    }

    /**
     * Validate business rules (số dư, tài khoản tồn tại, ...)
     * <p>
     * <b>Lưu ý:</b> Trong hệ thống thực tế, cần:
     * <ul>
     *     <li>Check tài khoản tồn tại (query từ Account table)</li>
     *     <li>Check số dư đủ (query từ Balance table)</li>
     *     <li>Check tài khoản có bị khóa không</li>
     *     <li>Check giới hạn giao dịch (daily limit, ...)</li>
     * </ul>
     * <p>
     * Ở đây chỉ implement basic validation, có thể mở rộng sau.
     *
     * @param fromAccount Số tài khoản nguồn
     * @param toAccount   Số tài khoản đích
     * @param amount      Số tiền
     * @throws BusinessException nếu business rules không thỏa mãn
     */
    private void validateBusinessRules(String fromAccount, String toAccount, BigDecimal amount) {
        // TODO: Implement actual business validation
        // - Check account exists
        // - Check account balance
        // - Check account status (active, locked, ...)
        // - Check transaction limits

        // Placeholder: Basic validation
        log.debug(TransactionConstants.LOG_VALIDATING_BUSINESS_RULES,
                LogMaskingUtil.maskAccount(fromAccount),
                LogMaskingUtil.maskAccount(toAccount),
                LogMaskingUtil.maskAmount(amount.toString()));
    }

    /**
     * Tạo bản ghi NỢ cho tài khoản nguồn
     * <p>
     * Bản ghi này ghi nhận số tiền bị trừ từ tài khoản nguồn
     *
     * @param transactionId Mã giao dịch
     * @param account       Số tài khoản nguồn
     * @param amount        Số tiền
     * @param time          Thời gian
     */
    private void createDebitRecord(String transactionId, String account, BigDecimal amount, LocalDateTime time) {
        try {
            // AES encrypt Account trước khi lưu vào database
            String encryptedAccount = encryptionService.encryptAccountForDatabase(account);

            TransactionHistory debitRecord = TransactionHistory.builder()
                    .transactionId(transactionId)
                    .account(encryptedAccount) // Đã được AES encrypt
                    .inDebt(amount)           // Số tiền nợ
                    .have(BigDecimal.ZERO)    // Không có số tiền có
                    .time(time)
                    .build();

            repository.save(debitRecord);

            log.debug(TransactionConstants.LOG_DEBIT_RECORD_CREATED,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(account));

        } catch (CryptoException e) {
            log.error(TransactionConstants.LOG_FAILED_TO_ENCRYPT_ACCOUNT_DEBIT,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(account),
                    e);
            throw e;
        } catch (Exception e) {
            log.error(TransactionConstants.LOG_FAILED_TO_CREATE_DEBIT,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(account),
                    LogMaskingUtil.maskAmount(amount.toString()),
                    LogMaskingUtil.maskTime(time.toString()),
                    e);
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    TransactionConstants.ERR_FAILED_TO_CREATE_DEBIT_RECORD,
                    e
            );
        }
    }

    /**
     * Tạo bản ghi CÓ cho tài khoản đích
     * <p>
     * Bản ghi này ghi nhận số tiền được cộng vào tài khoản đích
     *
     * @param transactionId Mã giao dịch
     * @param account       Số tài khoản đích
     * @param amount        Số tiền
     * @param time          Thời gian
     */
    private void createCreditRecord(String transactionId, String account, BigDecimal amount, LocalDateTime time) {
        try {
            // AES encrypt Account trước khi lưu vào database
            String encryptedAccount = encryptionService.encryptAccountForDatabase(account);

            TransactionHistory creditRecord = TransactionHistory.builder()
                    .transactionId(transactionId)
                    .account(encryptedAccount) // Đã được AES encrypt
                    .inDebt(BigDecimal.ZERO)  // Không có số tiền nợ
                    .have(amount)             // Số tiền có
                    .time(time)
                    .build();

            repository.save(creditRecord);

            log.debug(TransactionConstants.LOG_CREDIT_RECORD_CREATED,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(account));

        } catch (CryptoException e) {
            log.error(TransactionConstants.LOG_FAILED_TO_ENCRYPT_ACCOUNT_CREDIT,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(account),
                    e);
            throw e;
        } catch (Exception e) {
            log.error(TransactionConstants.LOG_FAILED_TO_CREATE_CREDIT,
                    LogMaskingUtil.maskTransactionId(transactionId),
                    LogMaskingUtil.maskAccount(account),
                    LogMaskingUtil.maskAmount(amount.toString()),
                    LogMaskingUtil.maskTime(time.toString()),
                    e);
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    TransactionConstants.ERR_FAILED_TO_CREATE_CREDIT_RECORD,
                    e
            );
        }
    }
}
