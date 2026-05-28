package com.banking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("🏦 Banking Backend API")
                .version("v1.0.0")
                .description("""
                    ## Hệ thống ngân hàng backend — RESTful API
                    
                    Dự án demo hệ thống ngân hàng xây dựng với **Spring Boot 3.4**, **PostgreSQL**, **RabbitMQ**.
                    
                    ### ✨ Tính năng nổi bật:
                    - 🔐 **JWT Authentication** — Stateless, RS256
                    - 🛡️ **Role-Based Access Control** — USER / ADMIN
                    - ⚡ **Rate Limiting** — Bucket4j, 20 req/phút mỗi user
                    - 🔁 **Idempotency Key** — Tránh giao dịch trùng lặp
                    - 🔒 **Pessimistic Locking** — An toàn khi concurrency cao
                    - 📊 **Ledger / Double-Entry Bookkeeping** — Mô hình kế toán kép
                    - 🐰 **RabbitMQ** — Async transaction logging
                    - 🏦 **External Transfer** — Hỗ trợ 6 ngân hàng (VCB, TCB, MB, BIDV, VP, ACB)
                    
                    ### 🚀 Test nhanh:
                    1. Dùng `/api/v1/auth/login` với `testuser1 / 123456`
                    2. Copy token → Click **Authorize** → Paste vào Bearer
                    3. Thử các API khác
                    
                    ### 👤 Tài khoản mẫu (tự động seed khi khởi động):
                    | Username | Password | Role | Account ID | Số dư |
                    |----------|----------|------|-----------|-------|
                    | testuser1 | 123456 | USER | ACC_SEED_A | 50,000,000 VND |
                    | testuser2 | 123456 | USER | ACC_SEED_B | 10,000 VND |
                    """)
                .contact(new Contact()
                    .name("Nguyen Minh Qua")
                    .url("https://github.com/quaminhNG/banking_be"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local Development Server")
            ))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Nhập JWT token từ `/api/v1/auth/login`. Format: `<token>` (không cần 'Bearer' prefix ở đây)")));
    }
}
