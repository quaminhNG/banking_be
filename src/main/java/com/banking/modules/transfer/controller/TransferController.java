package com.banking.modules.transfer.controller;

import com.banking.modules.transfer.dto.request.TransferRequest;
import com.banking.modules.transfer.dto.response.TransferResponse;
import com.banking.modules.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/api/v1/transaction/transfer")
@RequiredArgsConstructor
@Tag(name = "Transfer", description = "Chuyển tiền nội bộ và liên ngân hàng. Hỗ trợ Idempotency Key và Pessimistic Locking.")
public class TransferController {

    private final TransferService transferService;
    private final com.banking.modules.transfer.service.ExternalTransferService externalTransferService;

    @Operation(
        summary = "Thực hiện chuyển tiền",
        description = """
            Chuyển tiền giữa hai tài khoản:
            - **Nội bộ**: Không cần `toBankCode` — giao dịch trong hệ thống, dùng Pessimistic Lock để an toàn concurrency
            - **Liên ngân hàng**: Điền `toBankCode` (VD: VCB, TCB, MB, BIDV, VP, ACB)
            - **Idempotency**: Gửi cùng `idempotencyKey` nhiều lần sẽ trả về kết quả cũ, không tạo giao dịch trùng
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chuyển tiền thành công",
            content = @Content(schema = @Schema(implementation = TransferResponse.class),
                examples = @ExampleObject(value = "{\"transactionId\": \"TXN_...\", \"status\": \"SUCCESS\", \"amount\": 500000}"))),
        @ApiResponse(responseCode = "400", description = "Số dư không đủ hoặc tài khoản không tồn tại", content = @Content),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực (thiếu / sai JWT token)", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền (chỉ được chuyển từ tài khoản của chính mình)", content = @Content),
        @ApiResponse(responseCode = "429", description = "Rate limit bị vượt quá (Bucket4j - 20 req/phút)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response;
        if (request.getToBankCode() != null && !request.getToBankCode().isEmpty()) {
            response = externalTransferService.transferToExternal(request);
        } else {
            response = transferService.transfer(request);
            
        }
        return ResponseEntity.ok(response);
    }
}
