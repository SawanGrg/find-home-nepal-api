package com.beta.FindHome.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.beta.FindHome.exception.TokenVerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Service
public class RS256SignerService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    @Autowired
    public RS256SignerService(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    private Algorithm getAlgorithm() {
        return Algorithm.RSA256(publicKey, privateKey);
    }

    // Generate JWT using RS256 and private key
    public String generateJWT(String subject) {
        long now = System.currentTimeMillis();
        return JWT.create()
                .withSubject(subject)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + 1000 * 60 * 60 * 24)) // 24 hours
                .sign(getAlgorithm());
    }

    // Verify JWT and return decoded claims
    public DecodedJWT verifyJWT(String token) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new TokenVerificationException("Invalid JWT token", e);
        }
    }

    public String extractUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = verifyJWT(token);
            String subject = jwt.getSubject();
            if (subject == null || subject.isEmpty()) {
                throw new IllegalArgumentException("Token doesn't contain a valid username in subject claim");
            }
            return subject;
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Failed to extract username from token: " + e.getMessage(), e);
        }
    }

    public String extractUserPhoneNumber(String token) {
        try {
            DecodedJWT jwt = verifyJWT(token);
            String phoneNumber = jwt.getSubject(); // Assuming phone number is in subject
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                throw new IllegalArgumentException("Token doesn't contain a valid phone number in subject claim");
            }
            return phoneNumber;
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Failed to extract phone number from token: " + e.getMessage(), e);
        }
    }
}
