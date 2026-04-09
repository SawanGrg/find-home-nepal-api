package com.beta.FindHome.utils;

import com.beta.FindHome.filter.RateLimiterFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BucketCleanupSchedule {

    private final RateLimiterFilter rateLimiterFilter;

    @Autowired
    public BucketCleanupSchedule(RateLimiterFilter rateLimiterFilter) {
        this.rateLimiterFilter = rateLimiterFilter;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredBuckets() {
        rateLimiterFilter.cleanupExpiredBuckets();
    }
}