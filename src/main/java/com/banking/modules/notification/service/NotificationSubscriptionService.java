package com.banking.modules.notification.service;

import com.banking.exception.BankingException;
import com.banking.modules.auth.entity.User;
import com.banking.modules.auth.repository.UserRepository;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.notification.dto.request.SubscribeNotificationRequest;
import com.banking.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.banking.common.constants.NotificationSubscription.SUBSCRIPTION_FEE;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSubscriptionService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final LedgerService ledgerService;

    @Transactional
    public String subscribe(SubscribeNotificationRequest request) {
        User user = securityUtils.getCurrentUser();

        // Check if already subscribed to same email
        if (request.getEmail().equals(user.getEmail())) {
            throw new BankingException("You are already subscribed with this email.");
        }

        // Deduct fee using LedgerService (Throws exception if insufficient balance)
        String referenceId = "FEE-NOTI-" + UUID.randomUUID().toString().substring(0, 8);
        ledgerService.withdraw(user.getAccountId(), SUBSCRIPTION_FEE, referenceId);

        // Update user's email
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return "Successfully subscribed to email notifications. Fee deducted: 10,000 VND.";
    }
}
