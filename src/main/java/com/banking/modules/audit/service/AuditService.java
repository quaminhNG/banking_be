package com.banking.modules.audit.service;

import com.banking.modules.audit.entity.AuditLog;
import com.banking.modules.audit.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    final private AuditRepository auditRepository;
    public void createAuditLog(String requestId, String userId, String action, String metadata){
        AuditLog auditLog = new AuditLog();
        auditLog.setId(UUID.randomUUID().toString());
        auditLog.setRequestId(requestId);
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setMetadata(metadata);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditRepository.save(auditLog);
    }

}
