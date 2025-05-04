package com.atbm.core.encryption.symmetric;

public class DESEncryption extends SymmetricEncryption {

    public static final int KEY_SIZE = 56;

    public DESEncryption(String mode, String padding, int keySize) {
        super("DES", mode, padding, validateKeySize(keySize));
    }

    private static int validateKeySize(int keySize) {
        if (keySize != KEY_SIZE) {
            throw new IllegalArgumentException(
                    "Invalid key size for DES: " + keySize + ". DES only supports 56-bit keys.");
        }
        return keySize;
    }

    @Override
    public String getName() {
        return "DES";
    }
}