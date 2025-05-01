package com.atbm.core.encryption.traditional;

import java.security.Key;
import java.nio.charset.StandardCharsets;

public class AffineCipher extends TraditionalEncryption {
    public AffineCipher() {
        super("Affine");
    }

    private int modInverse(int a, int m) {
        a = a % m;
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1)
                return x;
        }
        throw new IllegalArgumentException("a và 26 không nguyên tố cùng nhau!");
    }

    public String encrypt(String plainText, String key) {
        String[] parts = key.split(",");
        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());
        StringBuilder result = new StringBuilder();
        for (char c : plainText.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                int x = c - 'A';
                int enc = (a * x + b) % 26;
                result.append((char) ('A' + enc));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public String decrypt(String cipherText, String key) {
        String[] parts = key.split(",");
        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());
        int a_inv = modInverse(a, 26);
        StringBuilder result = new StringBuilder();
        for (char c : cipherText.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                int y = c - 'A';
                int dec = (a_inv * (y - b + 26)) % 26;
                result.append((char) ('A' + dec));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        String keyStr = new String(key.getEncoded(), StandardCharsets.UTF_8);
        String plainText = new String(data, StandardCharsets.UTF_8);
        return encrypt(plainText, keyStr).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decrypt(byte[] data, Key key) throws Exception {
        String keyStr = new String(key.getEncoded(), StandardCharsets.UTF_8);
        String cipherText = new String(data, StandardCharsets.UTF_8);
        return decrypt(cipherText, keyStr).getBytes(StandardCharsets.UTF_8);
    }
}