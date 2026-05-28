package com.banking.modules.auth.controller;

import com.banking.modules.auth.dto.request.LoginRequest;
import com.banking.modules.auth.dto.request.RegisterRequest;
import com.banking.modules.auth.dto.response.AuthResponse;
import com.banking.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký và đăng nhập tài khoản, nhận JWT token để truy cập các API khác")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Đăng ký tài khoản mới",
        description = "Tạo tài khoản người dùng mới. Hệ thống sẽ tự động tạo một tài khoản ngân hàng (Account) liên kết."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Đăng ký thành công, trả về JWT token",
            content = @Content(schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiJ9...\", \"username\": \"testuser1\", \"role\": \"USER\", \"accountId\": \"ACC_xxxx\"}"))),
        @ApiResponse(responseCode = "400", description = "Username đã tồn tại hoặc dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "429", description = "Rate limit bị vượt quá (Bucket4j)", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
        summary = "Đăng nhập",
        description = "Xác thực thông tin và trả về JWT token. Token có hiệu lực trong 24 giờ. Dùng token này trong header `Authorization: Bearer <token>` cho tất cả các request khác."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
            content = @Content(schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiJ9...\", \"username\": \"testuser1\", \"role\": \"USER\", \"accountId\": \"ACC_SEED_A\"}"))),
        @ApiResponse(responseCode = "401", description = "Sai username hoặc password", content = @Content),
        @ApiResponse(responseCode = "429", description = "Rate limit bị vượt quá (Bucket4j)", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
