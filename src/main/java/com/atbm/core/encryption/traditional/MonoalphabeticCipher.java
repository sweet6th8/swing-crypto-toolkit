package com.atbm.core.encryption.traditional;

import java.security.Key;
import java.nio.charset.StandardCharsets;

public class MonoalphabeticCipher extends TraditionalEncryption {
    public MonoalphabeticCipher() {
        super("Monoalphabetic");
    }

    public String encrypt(String plainText, String key) {
        StringBuilder result = new StringBuilder();
        String upperKey = key.toUpperCase();
        for (char c : plainText.toCharArray()) {
            if (Character.isLetter(c)) {
                int base = Character.isLowerCase(c) ? 'a' : 'A';
                char upperC = Character.toUpperCase(c);
                char encrypted = upperKey.charAt(upperC - 'A');
                result.append(Character.isLowerCase(c) ? Character.toLowerCase(encrypted) : encrypted);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public String decrypt(String cipherText, String key) {
        StringBuilder result = new StringBuilder();
        String upperKey = key.toUpperCase();
        for (char c : cipherText.toCharArray()) {
            if (Character.isLetter(c)) {
                int base = Character.isLowerCase(c) ? 'a' : 'A';
                char upperC = Character.toUpperCase(c);
                int idx = upperKey.indexOf(upperC);
                result.append((char) (base + idx));
            } else {
                result.append(c);
            }
        }
        return result.toString();
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