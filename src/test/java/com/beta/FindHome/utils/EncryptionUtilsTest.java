package com.beta.FindHome.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@TestPropertySource(
        properties = {"encryption.salt.key=iiWc262yLV3cTALI1nWlOx6gPx6fj8xPIWPPuUIpygc="}
)
class EncryptionUtilsTest {

    private final EncryptionUtils encryptionUtils;

    public EncryptionUtilsTest(@Value("${encryption.salt.key}") String key) {
        this.encryptionUtils = new EncryptionUtils(key);
    }

    @Nested
    @DisplayName("Encryption & Decryption")
    class EncryptDecrypt {

        @Test
        @DisplayName("Encrypts and decrypts data correctly")
        void encryptsAndDecryptsCorrectly() {
            String original = "Secret Message";
            String encrypted = encryptionUtils.encrypt(original);

            assertNotNull(encrypted);
            assertNotEquals(original, encrypted);

            String decrypted = encryptionUtils.decrypt(encrypted);
            assertEquals(original, decrypted);
        }

        @Test
        @DisplayName("Handles empty string")
        void handlesEmptyString() {
            String encrypted = encryptionUtils.encrypt("");
            String decrypted = encryptionUtils.decrypt(encrypted);
            assertEquals("", decrypted);
        }

        @Test
        @DisplayName("Fails with tampered Base62")
        void failsWithInvalidBase62() {
            String badInput = "%%%INVALID%%%";
            RuntimeException e = assertThrows(RuntimeException.class, () -> encryptionUtils.decrypt(badInput));
            assertTrue(e.getMessage().contains("Decryption failed"));
        }

        @Test
        @DisplayName("Fails with truncated encrypted data")
        void failsWithTruncatedData() {
            String encrypted = encryptionUtils.encrypt("valid");
            String tampered = encrypted.substring(0, encrypted.length() / 2);
            RuntimeException e = assertThrows(RuntimeException.class, () -> encryptionUtils.decrypt(tampered));
            assertTrue(e.getMessage().contains("Decryption failed"));
        }

        @Test
        @DisplayName("Encrypts and decrypts unicode text")
        void supportsUnicode() {
            String input = "हाम्रो देश 🇳🇵";
            String encrypted = encryptionUtils.encrypt(input);
            String decrypted = encryptionUtils.decrypt(encrypted);
            assertEquals(input, decrypted);
        }
    }

    @Nested
    @DisplayName("Key Validation")
    class KeyValidation {

        @Test
        @DisplayName("Throws if key is not Base64")
        void throwsIfKeyNotBase64() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                    new EncryptionUtils("not-base64"));
            assertTrue(e.getMessage().contains("Invalid Base64 encryption key"));
        }

        @Test
        @DisplayName("Throws if key is not 32 bytes after decoding")
        void throwsIfKeyInvalidLength() {
            String tooShort = Base64.getEncoder()
                    .encodeToString("short".getBytes());
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                    new EncryptionUtils(tooShort));
            assertTrue(e.getMessage().contains("Invalid Base64 encryption key"));
        }
    }
}
