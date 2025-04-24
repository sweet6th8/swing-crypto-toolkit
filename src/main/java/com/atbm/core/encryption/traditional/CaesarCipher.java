package com.atbm.core.encryption.traditional;

import java.security.Key;

public class CaesarCipher extends TraditionalEncryption {

    private int shift;

    public CaesarCipher() {
        super("Caesar");
        this.shift = 0; // No default shift
    }

    public void setShift(int shift) {
        this.shift = shift; // Allow any integer shift
    }

    /**
     * Encrypts data using the Caesar cipher logic.
     * The Key parameter is ignored for Caesar cipher.
     */
    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        if (shift == 0 || data == null || data.length == 0)
            return data; // No change if shift is 0 or data is empty
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = shiftByte(data[i], shift);
        }
        return result;
    }

    /**
     * Decrypts data using the Caesar cipher logic.
     * The Key parameter is ignored for Caesar cipher.
     */
    @Override
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        if (shift == 0 || encryptedData == null || encryptedData.length == 0)
            return encryptedData; // No change if shift is 0 or data is empty
        // For decryption, we shift in the opposite direction
        byte[] result = new byte[encryptedData.length];
        for (int i = 0; i < encryptedData.length; i++) {
            result[i] = shiftByte(encryptedData[i], -shift);
        }
        return result;
    }

    // Helper method to shift a single byte (character)
    private byte shiftByte(byte b, int shiftAmount) {
        char c = (char) b;
        if (c >= 'a' && c <= 'z') {
            // Ensure positive modulo result for negative shifts
            int shifted = (c - 'a' + shiftAmount) % 26;
            if (shifted < 0)
                shifted += 26;
            return (byte) ('a' + shifted);
        } else if (c >= 'A' && c <= 'Z') {
            // Ensure positive modulo result for negative shifts
            int shifted = (c - 'A' + shiftAmount) % 26;
            if (shifted < 0)
                shifted += 26;
            return (byte) ('A' + shifted);
        }
        // Return unchanged if not an alphabet character
        return b;
    }

    // Caesar doesn't use standard modes/paddings in the same way
    @Override
    public String[] getSupportedModes() {
        return new String[] { "None" }; // Or just return super?
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "None" }; // Or just return super?
    }
}