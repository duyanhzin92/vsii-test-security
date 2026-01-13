package com.example.testsecurity.repository;

import com.example.testsecurity.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface cho TransactionHistory entity.
 * <p>
 * Cung cấp các method để truy vấn và lưu trữ transaction history trong database.
 */
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    /**
     * Kiểm tra xem transactionId đã tồn tại chưa.
     * <p>
     * Dùng để đảm bảo idempotency (mỗi transactionId chỉ được xử lý một lần)
     *
     * @param transactionId Mã giao dịch
     * @return true nếu transactionId đã tồn tại, false nếu chưa
     */
    boolean existsByTransactionId(String transactionId);
}

