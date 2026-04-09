package com.beta.FindHome.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class EncryptionUtils {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public EncryptionUtils(@Value("${encryption.salt.key}") String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("Key must be 256-bit (32 bytes) after Base64 decoding");
            }
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encryption key", e);
        }
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return toBase62(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encrypted) {
        try {
            byte[] combined = fromBase62(encrypted);

            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private static String toBase62(byte[] data) {
        StringBuilder sb = new StringBuilder();
        BigInteger bigInt = new BigInteger(1, data);
        while (bigInt.compareTo(BigInteger.ZERO) > 0) {
            java.math.BigInteger[] divmod = bigInt.divideAndRemainder(java.math.BigInteger.valueOf(62));
            sb.append(BASE62[divmod[1].intValue()]);
            bigInt = divmod[0];
        }
        return sb.reverse().toString();
    }

    private static byte[] fromBase62(String input) {
        java.math.BigInteger result = java.math.BigInteger.ZERO;
        for (char c : input.toCharArray()) {
            int index = new String(BASE62).indexOf(c);
            if (index == -1) throw new IllegalArgumentException("Invalid Base62 character: " + c);
            result = result.multiply(java.math.BigInteger.valueOf(62)).add(java.math.BigInteger.valueOf(index));
        }
        byte[] bytes = result.toByteArray();
        // Remove leading sign byte if present
        if (bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }
}
