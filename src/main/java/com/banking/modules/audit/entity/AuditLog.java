package com.banking.modules.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    private String id;

    @Column
    private String action;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
