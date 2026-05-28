package com.banking.modules.transaction.controller;

import com.banking.modules.transaction.dto.request.TransactionRequest;
import com.banking.modules.transaction.dto.response.TransactionResponse;
import com.banking.modules.transaction.service.TransactionService;
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
@RequestMapping("/api/v1/transaction/withdraw")
@RequiredArgsConstructor
@Tag(name = "Transaction")
public class WithdrawController {
    private final TransactionService transactionService;

    @Operation(
        summary = "Rút tiền từ tài khoản",
        description = """
            Rút tiền (Withdraw) từ tài khoản của người dùng đang đăng nhập.
            - Yêu cầu JWT token hợp lệ
            - Kiểm tra số dư trước khi thực hiện
            - Hỗ trợ `idempotencyKey` để tránh rút tiền trùng lặp
            - Giao dịch được ghi log async qua RabbitMQ
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rút tiền thành công",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Số dư không đủ hoặc tài khoản không tồn tại", content = @Content),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "429", description = "Rate limit bị vượt quá", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.processWithdraw(request));
    }
}

