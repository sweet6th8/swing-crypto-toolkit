package model.encryption;

import java.security.Key;

public class VigenereCipher extends TraditionalEncryption {

    private String keyword;

    public VigenereCipher() {
        super("Vigenere");
        this.keyword = ""; // No default keyword
    }

    public void setKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty.");
        }
        // Convert keyword to uppercase and ensure it only contains letters for
        // simplicity
        this.keyword = keyword.toUpperCase().replaceAll("[^A-Z]", "");
        if (this.keyword.isEmpty()) {
            throw new IllegalArgumentException("Keyword must contain at least one letter.");
        }
    }

    /**
     * Encrypts data using the Vigenere cipher logic.
     * The Key parameter is ignored.
     */
    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalStateException("Keyword must be set before encryption");
        }
        return process(data, true); // true for encrypt
    }

    /**
     * Decrypts data using the Vigenere cipher logic.
     * The Key parameter is ignored.
     */
    @Override
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalStateException("Keyword must be set before decryption");
        }
        return process(encryptedData, false); // false for decrypt
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
            byte resultByte = input[i]; // Default to original byte if not a letter

            if (inputChar >= 'A' && inputChar <= 'Z') {
                char keyChar = keyword.charAt(keywordIndex % keyword.length());
                int shift = keyChar - 'A';
                if (!encrypt) {
                    shift = 26 - shift; // Reverse shift for decryption
                }
                resultByte = (byte) ('A' + ((inputChar - 'A' + shift + 26) % 26));
                keywordIndex++; // Only increment keyword index for letters
            } else if (inputChar >= 'a' && inputChar <= 'z') {
                char keyChar = keyword.charAt(keywordIndex % keyword.length());
                int shift = keyChar - 'A'; // Keyword is uppercase
                if (!encrypt) {
                    shift = 26 - shift;
                }
                resultByte = (byte) ('a' + ((inputChar - 'a' + shift + 26) % 26));
                keywordIndex++;
            }
            // Non-alphabetic characters are passed through unchanged
            output[i] = resultByte;
        }
        return output;
    }

    // Vigenere doesn't use standard modes/paddings
    @Override
    public String[] getSupportedModes() {
        return new String[] { "None" };
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "None" };
    }
}