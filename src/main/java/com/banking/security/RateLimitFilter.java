package com.banking.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        Bucket bucket = null;

        if (uri.startsWith("/api/v1/auth/login")) {
            String key = getClientIP(request); // chưa log chưa có username nên dùng ip
            bucket = rateLimitService.resolveLoginBucket(key);

        } else if (uri.startsWith("/api/v1/transaction") || uri.startsWith("/api/v1/transfer")) {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // check anonymousUser
                                                                                          // SecurityContextHolder.getContext().getAuthentication();
                                                                                          // nếu là anonymousUser sẽ k
                                                                                          // cần .getName() vì rỗng
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                bucket = rateLimitService.resolveTransactionBucket(auth.getName());
            }
        }

        if (bucket != null) {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1); // trả về có hoặc k được xu nào
            if (!probe.isConsumed()) {
                // none xu => 429
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\"status\": 429, \"message\": \"Too many requests. Please try again later.\"}");
                return;
            }
            // add thông tin xu còn lại vào header
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        }

        filterChain.doFilter(request, response); // go next
    }

    private String getClientIP(HttpServletRequest request) { // nếu dùng request.getRemoteAddr() thì kq luôn là ip của
                                                             // Nginx

        String xfHeader = request.getHeader("X-Forwarded-For");// nếu có xfHeader thì lấy cái đầu tiên, nếu k thì
                                                               // request.getRemoteAddr() sẽ là ip của người dùng
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; // IP_Khách, IP_Cloudflare, IP_Nginx
        // cắt IP_Khách
    }
}
