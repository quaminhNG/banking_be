package com.banking.modules.auth.service;

import com.banking.exception.BankingException;
import com.banking.modules.account.dto.request.CreateAccountRequest;
import com.banking.modules.account.service.AccountService;
import com.banking.modules.audit.service.AuditService;
import com.banking.modules.auth.dto.request.LoginRequest;
import com.banking.modules.auth.dto.request.RegisterRequest;
import com.banking.modules.auth.dto.response.AuthResponse;
import com.banking.modules.auth.entity.User;
import com.banking.modules.auth.entity.UserRole;
import com.banking.modules.auth.repository.UserRepository;
import com.banking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BankingException("Username already exists");
        }

        String accountId = accountService.createAccount(new CreateAccountRequest(null));

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .accountId(accountId)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        auditService.createAuditLog(null, user.getId(), "REGISTER", "{\"accountId\":\"" + accountId + "\"}");

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name(), accountId);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BankingException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditService.createAuditLog(null, user.getId(), "LOGIN_FAILED", "{\"username\":\"" + request.getUsername() + "\"}");
            throw new BankingException("Invalid username or password");
        }

        auditService.createAuditLog(null, user.getId(), "LOGIN_SUCCESS", null);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name(), user.getAccountId());
    }
}
