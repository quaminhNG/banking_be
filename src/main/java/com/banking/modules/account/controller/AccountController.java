package com.banking.modules.account.controller;

import com.banking.modules.account.dto.request.CreateAccountRequest;
import com.banking.modules.account.dto.response.CreateAccountResponse;
import com.banking.modules.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "Quản lý tài khoản ngân hàng. Chỉ ADMIN mới được tạo tài khoản (Role-based access control).")
public class AccountController {
    private final AccountService accountService;

    @Operation(
        summary = "Tạo tài khoản ngân hàng (ADMIN only)",
        description = """
            Tạo một tài khoản ngân hàng mới trong hệ thống.
            - **Yêu cầu Role ADMIN** — trả về 403 nếu không đủ quyền
            - Tài khoản sẽ được khởi tạo với số dư 0 trong Ledger
            - Dùng để minh họa Role-Based Access Control (RBAC)
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tạo tài khoản thành công",
            content = @Content(schema = @Schema(implementation = CreateAccountResponse.class))),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không đủ quyền ADMIN", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        String accountId = accountService.createAccount(request);
        return ResponseEntity.ok(new CreateAccountResponse(accountId, "Account created successfully"));
    }
}
