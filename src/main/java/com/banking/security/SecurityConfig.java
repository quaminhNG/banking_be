package com.banking.security;

import com.banking.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // bật @PreAuthorize vidu: @PreAuthorize("hasRole('ADMIN')") gắn trước hàm
                      // xoaXXX(). nếu k có quyền ném 403
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;

    private static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();// JSON to Object
        MAPPER.registerModule(new JavaTimeModule());// nạp module thời gian
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // định dạng ISO-8601 ->
                                                                        // 2026-04-02T11:13:00
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // vì dùng JWT nên k cần dùng csrf
                .sessionManagement(sm -> sm
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT =
                                                                                                         // stateless k
                                                                                                         // nhớ gì bắt
                                                                                                         // buộc gửi
                                                                                                         // token mới
                                                                                                         // kèm theo khi
                                                                                                         // gửi request
                .exceptionHandling(ex -> ex
                                                // 401
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse error = ErrorResponse.build(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                    "Authentication required. Please provide a valid token.",
                                    request.getRequestURI());
                            response.getWriter().write(MAPPER.writeValueAsString(error));
                        })
                                                // 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse error = ErrorResponse.build(
                                    HttpStatus.FORBIDDEN.value(),
                                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                                    "Access denied. You do not have permission to access this resource.",
                                    request.getRequestURI());
                            response.getWriter().write(MAPPER.writeValueAsString(error));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/v1/accounts").hasRole("ADMIN")
                        // ngoài 2 điều trên thì phải đăng nhập
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, JwtFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}