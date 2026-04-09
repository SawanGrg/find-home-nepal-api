package com.beta.FindHome.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);

    @Value("${rate-limiter.limit:10}")
    private int requestLimit;

    @Value("${rate-limiter.time-window:PT1M}")  // Default 1 minute
    private String timeWindowString;

    @Value("${rate-limiter.bucket-ttl:PT5M}")  // Default 5 minutes
    private String bucketTtlString;

    private Duration timeWindow;
    private Duration bucketTtl;

    @PostConstruct
    public void init() {
        logger.info("Initializing RateLimiterFilter with requestLimit: {}, timeWindow: {}, bucketTtl: {}",
                requestLimit, timeWindowString, bucketTtlString);

        if (timeWindowString == null || timeWindowString.isEmpty()) {
            logger.error("timeWindowString is NULL or empty! Falling back to default: PT1M");
            timeWindowString = "PT1M";
        }

        if (bucketTtlString == null || bucketTtlString.isEmpty()) {
            logger.error("bucketTtlString is NULL or empty! Falling back to default: PT5M");
            bucketTtlString = "PT5M";
        }

        try {
            this.timeWindow = Duration.parse(timeWindowString.trim());
        } catch (DateTimeParseException e) {
            logger.error("Invalid timeWindow format: '{}', using default 1 minute.", timeWindowString, e);
            this.timeWindow = Duration.ofMinutes(1);
        }

        try {
            this.bucketTtl = Duration.parse(bucketTtlString.trim());
        } catch (DateTimeParseException e) {
            logger.error("Invalid bucketTtl format: '{}', using default 5 minutes.", bucketTtlString, e);
            this.bucketTtl = Duration.ofMinutes(5);
        }

        if (requestLimit <= 0) {
            logger.error("Invalid requestLimit: {}. Falling back to default: 10", requestLimit);
            requestLimit = 10;
        }

        logger.info("Final RateLimiter settings - requestLimit: {}, timeWindow: {}, bucketTtl: {}",
                requestLimit, timeWindow, bucketTtl);
    }





    private final Map<String, BucketWrapper> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        String requestPath = request.getRequestURI();

        BucketWrapper wrapper = buckets.compute(clientIp, (key, existingWrapper) -> {
            if (existingWrapper == null || isExpired(existingWrapper)) {
                logger.info("Creating new bucket for IP: {} with limit: {}/{}",
                        clientIp, requestLimit, formatDuration(timeWindow));
                return new BucketWrapper(createNewBucket(), Instant.now());
            }
            existingWrapper.lastAccessed = Instant.now();
            return existingWrapper;
        });

        // Get token count BEFORE consuming
        long availableBefore = wrapper.bucket.getAvailableTokens();

        if (wrapper.bucket.tryConsume(1)) {
            // Log after successful consumption
            logger.debug("Request allowed - IP: {}, Path: {}, Tokens: {}/{}, Consumed: 1",
                    clientIp, requestPath,
                    wrapper.bucket.getAvailableTokens(), requestLimit);
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            String errorMsg = String.format(
                    "{\"error\": \"Rate limit exceeded. Limit: %d requests per %s. Try again later.\"}",
                    requestLimit, formatDuration(timeWindow)
            );
            response.getWriter().write(errorMsg);

            logger.warn("Rate limit exceeded - IP: {}, Path: {}, Limit: {}/{}, Available before: {}",
                    clientIp, requestPath, requestLimit, formatDuration(timeWindow), availableBefore);
        }
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            logger.error("Received null duration in formatDuration.");
            return "unknown";
        }
        if (duration.toMinutes() > 0) {
            return duration.toMinutes() + " minute" + (duration.toMinutes() != 1 ? "s" : "");
        } else {
            return duration.getSeconds() + " second" + (duration.getSeconds() != 1 ? "s" : "");
        }
    }


    private boolean isExpired(BucketWrapper bucketWrapper) {
        if (bucketTtl == null) {
            logger.error("bucketTtl is NULL in isExpired()! Using default 5 minutes.");
            bucketTtl = Duration.ofMinutes(5);
        }

        boolean expired = Duration.between(bucketWrapper.lastAccessed, Instant.now()).compareTo(bucketTtl) > 0;

        if (expired) {
            logger.debug("Bucket expired for lastAccessed: {}", bucketWrapper.lastAccessed);
        }

        return expired;
    }

    private Bucket createNewBucket() {
        if (timeWindow == null || requestLimit <= 0) {
            logger.error("Rate limiter time window is null or requestLimit is invalid ({}). Setting defaults.", requestLimit);
            timeWindow = Duration.ofMinutes(1);
            requestLimit = 10;
        }

        Bandwidth limit = Bandwidth.simple(requestLimit, timeWindow);
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }


    public void cleanupExpiredBuckets() {
        int initialSize = buckets.size();
        buckets.entrySet().removeIf(entry -> isExpired(entry.getValue()));
        int removed = initialSize - buckets.size();
        if (removed > 0) {
            logger.info("Cleaned up {} expired buckets. Current active buckets: {}", removed, buckets.size());
        }
    }

    private static class BucketWrapper {
        final Bucket bucket;
        Instant lastAccessed;

        BucketWrapper(Bucket bucket, Instant lastAccessed) {
            this.bucket = bucket;
            this.lastAccessed = lastAccessed;
        }
    }
}