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
public class SmsRateLimiterFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(SmsRateLimiterFilter.class);

    @Value("${sms-rate-limiter.limit}")
    private int requestLimit;

    @Value("${sms-rate-limiter.time-window}")
    private String timeWindowString;

    @Value("${sms-rate-limiter.bucket-ttl}")
    private String bucketTtlString;

    private Duration timeWindow;
    private Duration bucketTtl;


    // Update the rate limited paths to match actual endpoints
    private static final String[] RATE_LIMITED_PATHS = {
            "/api/v1/auth/otp/send/",
            "/api/v1/auth/otp/resend/"
    };

    private final Map<String, BucketWrapper> buckets = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("Initializing SMS rate limiter filter with configuration: requestLimit={}, timeWindow='{}', bucketTtl='{}'",
                requestLimit, timeWindowString, bucketTtlString);

        if (timeWindowString == null || timeWindowString.isEmpty()) {
            logger.error("Invalid or missing timeWindow configuration. Falling back to default: PT1M");
            timeWindowString = "PT1M";
        }

        if (bucketTtlString == null || bucketTtlString.isEmpty()) {
            logger.error("Invalid or missing bucketTtl configuration. Falling back to default: PT5M");
            bucketTtlString = "PT5M";
        }

        try {
            this.timeWindow = Duration.parse(timeWindowString.trim());
        } catch (DateTimeParseException e) {
            logger.error("Invalid timeWindow format: '{}'. Using default 1 minute.", timeWindowString, e);
            this.timeWindow = Duration.ofMinutes(1);
        }

        try {
            this.bucketTtl = Duration.parse(bucketTtlString.trim());
        } catch (DateTimeParseException e) {
            logger.error("Invalid bucketTtl format: '{}'. Using default 5 minutes.", bucketTtlString, e);
            this.bucketTtl = Duration.ofMinutes(5);
        }

        if (requestLimit <= 0) {
            logger.error("Configured requestLimit={} is invalid. Using default: 10", requestLimit);
            requestLimit = 10;
        }

        logger.info("Finalized SMS rate limiter settings - requestLimit={}, timeWindow={}, bucketTtl={}",
                requestLimit, timeWindow, bucketTtl);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Check if any of the defined paths match the beginning of the request URI
        for (String rateLimitedPath : RATE_LIMITED_PATHS) {
            if (path.startsWith(rateLimitedPath)) {
                return false; // Don't skip filtering for these paths
            }
        }
        return true; // Skip filtering for all other paths
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        String requestPath = request.getRequestURI();

        logger.info("SMS Rate Limiter evaluating request: {} {} from IP: {}",
                request.getMethod(), requestPath, clientIp);

        if (shouldNotFilter(request)) {
            logger.debug("SMS Rate Limiter skipping request: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("SMS Rate Limiter processing request: {}", requestPath);

        BucketWrapper wrapper = buckets.compute(clientIp, (key, existingWrapper) -> {
            if (existingWrapper == null || isExpired(existingWrapper)) {
                logger.info("Creating new SMS bucket for IP [{}] with limit {}/{}",
                        clientIp, requestLimit, formatDuration(timeWindow));
                return new BucketWrapper(createNewBucket(), Instant.now());
            }
            existingWrapper.lastAccessed = Instant.now();
            return existingWrapper;
        });

        long availableTokensBefore = wrapper.bucket.getAvailableTokens();

        if (wrapper.bucket.tryConsume(1)) {
            logger.info("SMS Request from IP [{}] allowed. Remaining tokens: {}/{}",
                    clientIp, wrapper.bucket.getAvailableTokens(), requestLimit);
            filterChain.doFilter(request, response);
        } else {
            logger.warn("SMS Rate limit exceeded for IP [{}]. Denying SMS request to [{}]", clientIp, requestPath);
            response.setStatus(429);
            response.setContentType("application/json");
            String errorMsg = String.format(
                    "{\"error\": \"SMS rate limit exceeded. You can only request %d OTPs per %s.\", " +
                            "\"retryAfter\": \"%s\"}",
                    requestLimit,
                    formatDuration(timeWindow),
                    formatDuration(timeWindow.minus(Duration.between(
                            Instant.now().minus(timeWindow),
                            Instant.now()
                    )))
            );
            response.getWriter().write(errorMsg);
        }
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            logger.error("formatDuration() called with null duration.");
            return "unknown";
        }
        if (duration.toHours() > 0) {
            return duration.toHours() + " hour" + (duration.toHours() != 1 ? "s" : "");
        } else {
            return duration.toMinutes() + " minute" + (duration.toMinutes() != 1 ? "s" : "");
        }
    }

    private boolean isExpired(BucketWrapper bucketWrapper) {
        if (bucketTtl == null) {
            logger.warn("bucketTtl is NULL in isExpired(). Falling back to default TTL: 1 hour");
            bucketTtl = Duration.ofHours(1);
        }
        return Duration.between(bucketWrapper.lastAccessed, Instant.now()).compareTo(bucketTtl) > 0;
    }

    private Bucket createNewBucket() {
        if (timeWindow == null || requestLimit <= 0) {
            logger.warn("Rate limiter misconfigured (timeWindow: {}, requestLimit: {}). Falling back to defaults.",
                    timeWindow, requestLimit);
            timeWindow = Duration.ofMinutes(1);
            requestLimit = 10;
        }

        Bandwidth limit = Bandwidth.simple(requestLimit, timeWindow);
        return Bucket4j.builder().addLimit(limit).build();
    }

    public void cleanupExpiredBuckets() {
        int initialSize = buckets.size();
        buckets.entrySet().removeIf(entry -> isExpired(entry.getValue()));
        int removed = initialSize - buckets.size();
        if (removed > 0) {
            logger.info("Expired SMS rate limiter buckets cleanup: {} removed, {} remaining.", removed, buckets.size());
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
