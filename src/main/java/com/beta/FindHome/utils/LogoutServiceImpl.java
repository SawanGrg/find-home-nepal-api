package com.beta.FindHome.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class LogoutServiceImpl {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;

    @Autowired
    public LogoutServiceImpl(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    public String logoutUser(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                return "❌ Missing or invalid Authorization header";
            }

            // Step 1: Verify the token is valid and not expired
            DecodedJWT decodedJWT = jwtUtils.verifyAndDecodeToken(token);

            // Step 2: Check if token is already blacklisted (optional)
            if (redisTemplate.opsForValue().get(token) != null) {
                return "⚠️ Token is already blacklisted";
            }

            // Step 3: Calculate remaining TTL (Time-To-Live)
            Date expiration = decodedJWT.getExpiresAt();
            long ttl = Math.max(0, (expiration.getTime() - System.currentTimeMillis()) / 1000);

            // Step 4: Add to Redis blacklist
            redisTemplate.opsForValue().set(token, "blacklisted", ttl, TimeUnit.SECONDS);

            return "Logout successful. Token blacklisted.";
        } catch (JWTVerificationException e) {
            return "Invalid or expired JWT: " + e.getMessage();
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}