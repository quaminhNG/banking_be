package com.banking.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    // Cache lưu trữ các bucket theo key (IP hoặc Username)
    // Sau 10 phút không hoạt động sẽ tự xóa để giải phóng bộ nhớ
    private final LoadingCache<String, Bucket> loginBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .build(key -> createNewBucket(5, Duration.ofMinutes(1))); // 5 lần/phút cho Login

    private final LoadingCache<String, Bucket> transactionBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .build(key -> createNewBucket(20, Duration.ofMinutes(1))); // 20 lần/phút cho Giao dịch

    public Bucket resolveLoginBucket(String key) {
        return loginBuckets.get(key);
    }

    public Bucket resolveTransactionBucket(String key) {
        return transactionBuckets.get(key);
    }

    private Bucket createNewBucket(long capacity, Duration period) {
        Refill refill = Refill.greedy(capacity, period);
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
