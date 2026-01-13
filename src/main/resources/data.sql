-- =====================================================
-- Banking Transaction System - Sample Data
-- =====================================================
-- 
-- Script này chứa dữ liệu mẫu để test hệ thống banking transaction.
-- 
-- LƯU Ý QUAN TRỌNG:
-- - Account numbers trong bảng này đã được mã hóa bằng AES-256/GCM
-- - Để insert data mới, cần mã hóa Account number trước khi insert
-- - Sử dụng EncryptionService.encryptAccountForDatabase() để mã hóa Account
-- 
-- =====================================================

-- Xóa dữ liệu cũ (nếu có)
DELETE FROM transaction_history;

-- Reset auto increment
ALTER TABLE transaction_history AUTO_INCREMENT = 1;

-- =====================================================
-- Sample Transaction Data
-- =====================================================
-- 
-- Mỗi giao dịch chuyển khoản tạo 2 bản ghi:
-- 1. Bản ghi NỢ cho tài khoản nguồn (InDebt = amount, Have = 0)
-- 2. Bản ghi CÓ cho tài khoản đích (InDebt = 0, Have = amount)
-- 
-- Account numbers đã được mã hóa AES với key từ application.yaml
-- Format: Base64(IV + Encrypted Account + Auth Tag)
-- 
-- =====================================================

-- Giao dịch 1: Chuyển 1,000,000 VND từ tài khoản 1234567890 → 9876543210
-- Transaction ID: TXN20240115001
INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES
('TXN20240115001', 'ENCRYPTED_ACCOUNT_1234567890', 1000000.00, 0.00, '2024-01-15 10:30:00'),
('TXN20240115001', 'ENCRYPTED_ACCOUNT_9876543210', 0.00, 1000000.00, '2024-01-15 10:30:00');

-- Giao dịch 2: Chuyển 500,000 VND từ tài khoản 9876543210 → 1111111111
-- Transaction ID: TXN20240115002
INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES
('TXN20240115002', 'ENCRYPTED_ACCOUNT_9876543210', 500000.00, 0.00, '2024-01-15 11:15:00'),
('TXN20240115002', 'ENCRYPTED_ACCOUNT_1111111111', 0.00, 500000.00, '2024-01-15 11:15:00');

-- Giao dịch 3: Chuyển 2,500,000 VND từ tài khoản 1111111111 → 1234567890
-- Transaction ID: TXN20240115003
INSERT INTO transaction_history (transaction_id, account, in_debt, have, time) VALUES
('TXN20240115003', 'ENCRYPTED_ACCOUNT_1111111111', 2500000.00, 0.00, '2024-01-15 14:20:00'),
('TXN20240115003', 'ENCRYPTED_ACCOUNT_1234567890', 0.00, 2500000.00, '2024-01-15 14:20:00');

-- =====================================================
-- LƯU Ý:
-- =====================================================
-- 
-- Các giá trị 'ENCRYPTED_ACCOUNT_*' ở trên chỉ là placeholder.
-- Trong thực tế, bạn cần:
-- 
-- 1. Chạy ứng dụng Spring Boot
-- 2. Sử dụng EncryptionService để mã hóa Account numbers
-- 3. Thay thế các placeholder bằng giá trị đã mã hóa thực tế
-- 
-- Hoặc sử dụng script Java để generate data (xem DataGenerator.java)
-- 
-- =====================================================
