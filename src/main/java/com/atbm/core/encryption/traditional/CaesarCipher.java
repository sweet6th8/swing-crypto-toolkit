package com.atbm.core.encryption.traditional;

import java.security.Key;

public class CaesarCipher extends TraditionalEncryption {

    private int shift;

    public CaesarCipher() {
        super("Caesar");
        this.shift = 0;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    // Mã hóa dữ liệu
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

    // Giải mã dữ liệu
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

    // Phương thức dịch byte, chỉ dịch các ký tự alphabet
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

    // Caesar không sử dụng các mode/padding chuẩn
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
            result.append(shiftChar(c, shiftValue));
        }
        return result.toString();
    }

    @Override
    public String decrypt(String cipherText, String key) {
        int shiftValue = Integer.parseInt(key.trim());
        StringBuilder result = new StringBuilder();
        for (char c : cipherText.toCharArray()) {
            result.append(shiftChar(c, -shiftValue));
        }
        return result.toString();
    }

    private char shiftChar(char c, int shiftAmount) {
        if (c >= 'a' && c <= 'z') {
            int shifted = (c - 'a' + shiftAmount) % 26;
            if (shifted < 0)
                shifted += 26;
            return (char) ('a' + shifted);
        } else if (c >= 'A' && c <= 'Z') {
            int shifted = (c - 'A' + shiftAmount) % 26;
            if (shifted < 0)
                shifted += 26;
            return (char) ('A' + shifted);
        }
        return c;
    }
}