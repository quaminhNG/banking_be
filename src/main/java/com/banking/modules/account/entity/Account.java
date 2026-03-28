package com.banking.modules.account.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
