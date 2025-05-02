package com.atbm.core.encryption.symmetric;

import javax.crypto.SecretKey;
import java.security.Key;

public class DESEncryption extends SymmetricEncryption {

    // DES key size is fixed at 56 bits (64 bits with parity)
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

    @Override
    public String[] getSupportedModes() {
        return new String[] { "ECB", "CBC" };
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "PKCS5Padding", "NoPadding" };
    }
}