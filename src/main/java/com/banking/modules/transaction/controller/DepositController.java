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
@RequestMapping("/api/v1/transaction/deposit")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Nạp tiền (Deposit) và rút tiền (Withdraw) vào/ra tài khoản ngân hàng. Hỗ trợ Idempotency Key.")
public class DepositController {
    private final TransactionService transactionService;

    @Operation(
        summary = "Nạp tiền vào tài khoản",
        description = """
            Nạp tiền (Deposit) vào tài khoản chỉ định.
            - Yêu cầu JWT token hợp lệ trong header
            - Hỗ trợ `idempotencyKey` để tránh nạp tiền trùng lặp
            - Giao dịch được ghi log async qua RabbitMQ
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nạp tiền thành công",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Tài khoản không tồn tại hoặc dữ liệu không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "429", description = "Rate limit bị vượt quá", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.processDeposit(request));
    }
}

