package com.atbm.core.encryption.traditional;

import java.security.Key;
import java.nio.charset.StandardCharsets;

public class HillCipher extends TraditionalEncryption {
    public HillCipher() {
        super("Hill");
    }

    private int modInverse(int a, int m) {
        a = a % m;
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1)
                return x;
        }
        throw new IllegalArgumentException("Không có nghịch đảo modular!");
    }

    private int[][] parseKey(String key) {
        String[] nums = key.split(",");
        if (nums.length != 4)
            throw new IllegalArgumentException("Key phải có 4 số cho ma trận 2x2!");
        int[][] matrix = new int[2][2];
        for (int i = 0; i < 4; i++) {
            matrix[i / 2][i % 2] = Integer.parseInt(nums[i].trim());
        }
        return matrix;
    }

    public String encrypt(String plainText, String key) {
        int[][] k = parseKey(key);
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < plainText.length()) {
            // Skip non-alphabetic characters
            while (i < plainText.length() && !Character.isLetter(plainText.charAt(i))) {
                result.append(plainText.charAt(i));
                i++;
            }

            if (i >= plainText.length())
                break;

            // Get first character of pair and remember its case
            char c1 = plainText.charAt(i);
            boolean isC1Lower = Character.isLowerCase(c1);
            c1 = Character.toUpperCase(c1);
            i++;

            // Skip non-alphabetic characters
            while (i < plainText.length() && !Character.isLetter(plainText.charAt(i))) {
                result.append(plainText.charAt(i));
                i++;
            }

            if (i >= plainText.length()) {
                // If we have only one letter at the end, add padding
                result.append(isC1Lower ? Character.toLowerCase(c1) : c1);
                result.append('X');
                break;
            }

            // Get second character of pair and remember its case
            char c2 = plainText.charAt(i);
            boolean isC2Lower = Character.isLowerCase(c2);
            c2 = Character.toUpperCase(c2);
            i++;

            // Process the pair
            int[] vec = { c1 - 'A', c2 - 'A' };
            int enc1 = (k[0][0] * vec[0] + k[0][1] * vec[1]) % 26;
            int enc2 = (k[1][0] * vec[0] + k[1][1] * vec[1]) % 26;
            if (enc1 < 0)
                enc1 += 26;
            if (enc2 < 0)
                enc2 += 26;

            // Restore original case
            result.append(isC1Lower ? Character.toLowerCase((char) ('A' + enc1)) : (char) ('A' + enc1));
            result.append(isC2Lower ? Character.toLowerCase((char) ('A' + enc2)) : (char) ('A' + enc2));
        }

        return result.toString();
    }

    public String decrypt(String cipherText, String key) {
        int[][] k = parseKey(key);
        int det = (k[0][0] * k[1][1] - k[0][1] * k[1][0]) % 26;
        if (det < 0)
            det += 26;
        int detInv = modInverse(det, 26);
        int[][] inv = {
                { k[1][1] * detInv % 26, (-k[0][1] + 26) * detInv % 26 },
                { (-k[1][0] + 26) * detInv % 26, k[0][0] * detInv % 26 }
        };
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < cipherText.length()) {
            // Skip non-alphabetic characters
            while (i < cipherText.length() && !Character.isLetter(cipherText.charAt(i))) {
                result.append(cipherText.charAt(i));
                i++;
            }

            if (i >= cipherText.length())
                break;

            // Get first character of pair and remember its case
            char c1 = cipherText.charAt(i);
            boolean isC1Lower = Character.isLowerCase(c1);
            c1 = Character.toUpperCase(c1);
            i++;

            // Skip non-alphabetic characters
            while (i < cipherText.length() && !Character.isLetter(cipherText.charAt(i))) {
                result.append(cipherText.charAt(i));
                i++;
            }

            if (i >= cipherText.length()) {
                // If we have only one letter at the end, just append it
                result.append(isC1Lower ? Character.toLowerCase(c1) : c1);
                break;
            }

            // Get second character of pair and remember its case
            char c2 = cipherText.charAt(i);
            boolean isC2Lower = Character.isLowerCase(c2);
            c2 = Character.toUpperCase(c2);
            i++;

            // Process the pair
            int[] vec = { c1 - 'A', c2 - 'A' };
            int dec1 = (inv[0][0] * vec[0] + inv[0][1] * vec[1]) % 26;
            int dec2 = (inv[1][0] * vec[0] + inv[1][1] * vec[1]) % 26;
            if (dec1 < 0)
                dec1 += 26;
            if (dec2 < 0)
                dec2 += 26;

            // Restore original case
            result.append(isC1Lower ? Character.toLowerCase((char) ('A' + dec1)) : (char) ('A' + dec1));
            result.append(isC2Lower ? Character.toLowerCase((char) ('A' + dec2)) : (char) ('A' + dec2));
        }

        String res = result.toString();
        // Remove padding 'X' if it was added during encryption
        if (res.endsWith("X") && res.length() > 1) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) {
        String keyStr = new String(key.getEncoded(), StandardCharsets.UTF_8);
        String plainText = new String(data, StandardCharsets.UTF_8);
        return encrypt(plainText, keyStr).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decrypt(byte[] data, Key key) {
        String keyStr = new String(key.getEncoded(), StandardCharsets.UTF_8);
        String cipherText = new String(data, StandardCharsets.UTF_8);
        return decrypt(cipherText, keyStr).getBytes(StandardCharsets.UTF_8);
    }
}