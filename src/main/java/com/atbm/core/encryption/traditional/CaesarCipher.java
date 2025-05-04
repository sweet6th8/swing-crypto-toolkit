package com.atbm.core.encryption.traditional;

import java.security.Key;

// Class này mã hóa và giải mã dữ liệu sử dụng Caesar Cipher
public class CaesarCipher extends TraditionalEncryption {

    private int shift;

    public CaesarCipher() {
        super("Caesar");
        this.shift = 0;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    // Mã hóa
    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        if (shift == 0 || data == null || data.length == 0)
            return data;
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = shiftByte(data[i], shift);
        }
        return result;
    }

    // Giải mã
    @Override
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        if (shift == 0 || encryptedData == null || encryptedData.length == 0)
            return encryptedData;
        byte[] result = new byte[encryptedData.length];
        for (int i = 0; i < encryptedData.length; i++) {
            result[i] = shiftByte(encryptedData[i], -shift);
        }
        return result;
    }

    // Phương thức dịch byte
    private byte shiftByte(byte b, int shiftAmount) {
        char c = (char) b;
        if (c >= 'a' && c <= 'z') {
            int shifted = (c - 'a' + shiftAmount) % 26;
            if (shifted < 0)
                shifted += 26;
            return (byte) ('a' + shifted);
        } else if (c >= 'A' && c <= 'Z') {
            int shifted = (c - 'A' + shiftAmount) % 26;
            if (shifted < 0)
                shifted += 26;
            return (byte) ('A' + shifted);
        }
        return b;
    }

    // Caesar không sử dụng các mode/padding
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
        int shiftValue = Integer.parseInt(key.trim());
        StringBuilder result = new StringBuilder();
        for (char c : plainText.toCharArray()) {
            if (Character.isLetter(c)) {
                int base = Character.isLowerCase(c) ? 'a' : 'A';
                int x = c - base;
                int shifted = (x + shiftValue) % 26;
                if (shifted < 0)
                    shifted += 26;
                result.append((char) (base + shifted));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    public String decrypt(String cipherText, String key) {
        int shiftValue = Integer.parseInt(key.trim());
        StringBuilder result = new StringBuilder();
        for (char c : cipherText.toCharArray()) {
            if (Character.isLetter(c)) {
                int base = Character.isLowerCase(c) ? 'a' : 'A';
                int x = c - base;
                int shifted = (x - shiftValue) % 26;
                if (shifted < 0)
                    shifted += 26;
                result.append((char) (base + shifted));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}