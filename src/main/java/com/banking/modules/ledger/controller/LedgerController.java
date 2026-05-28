package com.banking.modules.ledger.controller;

import com.banking.modules.ledger.entity.BalanceSnapshot;
import com.banking.modules.ledger.repository.BalanceSnapshotRepository;
import com.banking.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "Xem số dư tài khoản theo mô hình Ledger (Double-entry bookkeeping). Mỗi giao dịch đều có entry ghi nhật ký.")
public class LedgerController {
    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final SecurityUtils securityUtils;

    @Operation(
        summary = "Xem số dư tài khoản",
        description = """
            Lấy số dư hiện tại của tài khoản theo mô hình Ledger.
            - Chỉ được xem số dư của tài khoản thuộc về chính mình
            - Số dư được tính từ snapshot + các ledger entries chưa snapshot
            - Minh họa kiến trúc Ledger / Double-Entry Bookkeeping
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trả về thông tin số dư",
            content = @Content(schema = @Schema(implementation = BalanceSnapshot.class),
                examples = @ExampleObject(value = "{\"accountId\": \"ACC_SEED_A\", \"balance\": 50000000.00, \"updatedAt\": \"2026-04-08T10:00:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền xem tài khoản của người khác", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản", content = @Content)
    })
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<BalanceSnapshot> getBalance(
            @Parameter(description = "ID tài khoản cần xem số dư (VD: ACC_SEED_A)", example = "ACC_SEED_A")
            @PathVariable String accountId) {
        // Chỉ cho phép xem balance của chính mình (ADMIN bypass qua @PreAuthorize nếu cần)
        securityUtils.verifyAccountOwnership(accountId);

        return balanceSnapshotRepository.findById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
