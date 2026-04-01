package com.banking.security;

import com.banking.exception.BankingException;
import com.banking.modules.auth.entity.User;
import com.banking.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Lấy user đang đăng nhập từ SecurityContext.
     */
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName(); // SecurityContextHolder.getContext().getAuthentication().getName();
                                                                                            // lấy auth ra
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("Authenticated user not found"));
    }

    /**
     * Kiểm tra accountId có thuộc về user đang đăng nhập không.
     * Nếu không → ném 403 Forbidden.
     */
    public void verifyAccountOwnership(String accountId) {
        User user = getCurrentUser();
        if (!accountId.equals(user.getAccountId())) {
            throw new BankingException("Access denied: not your account", HttpStatus.FORBIDDEN);
        }
    }
}
