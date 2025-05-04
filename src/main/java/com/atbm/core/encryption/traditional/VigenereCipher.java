package com.atbm.core.encryption.traditional;

import java.security.Key;

// Class này mã hóa và giải mã dữ liệu sử dụng Vigenere Cipher
public class VigenereCipher extends TraditionalEncryption {

    private String keyword;

    public VigenereCipher() {
        super("Vigenere");
        this.keyword = "";
    }

    public void setKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty.");
        }
        this.keyword = keyword.toUpperCase().replaceAll("[^A-Z]", "");
        if (this.keyword.isEmpty()) {
            throw new IllegalArgumentException("Keyword must contain at least one letter.");
        }
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalStateException("Keyword must be set before encryption");
        }
        return process(data, true);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalStateException("Keyword must be set before decryption");
        }
        return process(encryptedData, false);
    }

    private byte[] process(byte[] input, boolean encrypt) {
        if (input == null || input.length == 0) {
            return input;
        }
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalStateException("Keyword must be set before processing");
        }

        byte[] output = new byte[input.length];
        int keywordIndex = 0;

        for (int i = 0; i < input.length; i++) {
            char inputChar = (char) input[i];
            byte resultByte = input[i];

            if (inputChar >= 'A' && inputChar <= 'Z') {
                char keyChar = keyword.charAt(keywordIndex % keyword.length());
                int shift = keyChar - 'A';
                if (!encrypt) {
                    shift = 26 - shift;
                }
                resultByte = (byte) ('A' + ((inputChar - 'A' + shift + 26) % 26));
                keywordIndex++;
            } else if (inputChar >= 'a' && inputChar <= 'z') {
                char keyChar = keyword.charAt(keywordIndex % keyword.length());
                int shift = keyChar - 'A';
                if (!encrypt) {
                    shift = 26 - shift;
                }
                resultByte = (byte) ('a' + ((inputChar - 'a' + shift + 26) % 26));
                keywordIndex++;
            }
            output[i] = resultByte;
        }
        return output;
    }

    @Override
    public String[] getSupportedModes() {
        return new String[] { "None" };
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "None" };
    }

    @Override
    public String encrypt(String plainText, String key) {
        String keyword = key.toUpperCase().replaceAll("[^A-Z]", "");
        StringBuilder result = new StringBuilder();
        int keywordIndex = 0;
        for (char c : plainText.toCharArray()) {
            if (Character.isLetter(c)) {
                int base = Character.isLowerCase(c) ? 'a' : 'A';
                char keyChar = keyword.charAt(keywordIndex % keyword.length());
                int shift = keyChar - 'A';
                int x = c - base;
                int shifted = (x + shift) % 26;
                result.append((char) (base + shifted));
                keywordIndex++;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    public String decrypt(String cipherText, String key) {
        String keyword = key.toUpperCase().replaceAll("[^A-Z]", "");
        StringBuilder result = new StringBuilder();
        int keywordIndex = 0;
        for (char c : cipherText.toCharArray()) {
            if (Character.isLetter(c)) {
                int base = Character.isLowerCase(c) ? 'a' : 'A';
                char keyChar = keyword.charAt(keywordIndex % keyword.length());
                int shift = 26 - (keyChar - 'A');
                int x = c - base;
                int shifted = (x + shift) % 26;
                result.append((char) (base + shifted));
                keywordIndex++;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}