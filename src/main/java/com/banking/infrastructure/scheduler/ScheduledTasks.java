package com.banking.infrastructure.scheduler;

import com.banking.modules.audit.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final AuditRepository auditRepository;

    /**
     * Tự động dọn dẹp các bản ghi Audit Log cũ hơn 30 ngày.
     * Chạy vào 03:00 AM hàng ngày.
     * Cron expression: "0giay 0phut 3gio *moingaytrongthang *moingaytrongnam
     * ?khongquantrongthumay"
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldAuditLogs() {
        log.info("Starting scheduled cleanup of old audit logs...");
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(30);
        try {
            auditRepository.deleteByCreatedAtBefore(expiryDate);
            log.info("Successfully cleaned up audit logs older than {}", expiryDate);
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs: {}", e.getMessage());
        }
    }

    /**
     * Báo cáo nhanh số lượng giao dịch trong ngày.
     * Chạy định kỳ mỗi 1 tiếng để log thông tin.
     */
    @Scheduled(fixedRate = 3600000)
    public void printTransactionSummary() {
        log.info("System Heartbeat: Audit Log count currently is {}", auditRepository.count());
    }
}
