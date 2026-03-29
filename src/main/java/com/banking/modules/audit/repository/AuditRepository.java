package com.banking.modules.audit.repository;

import com.banking.modules.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByUserId(String userId);
}
