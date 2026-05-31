package com.github.codesm27.codereview.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

@Component
@Slf4j
public class HmacSignatureValidator {

    @Value("${app.github.webhook.secret}")
    private String webhookSecret;

    public boolean isValidSignature(String payloadBody, String githubSignature) {
        if (githubSignature == null || !githubSignature.startsWith("sha256=")) {
            return false;
        }

        try {
            String actualSignature = githubSignature.substring(7);
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] rawHmac = mac.doFinal(payloadBody.getBytes());
            String expectedSignature = bytesToHex(rawHmac);

            // Use MessageDigest.isEqual for constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(expectedSignature.getBytes(), actualSignature.getBytes());
        } catch (Exception e) {
            log.error("Error validating HMAC signature", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
