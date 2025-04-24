package com.atbm.core.encryption.symmetric;

import javax.crypto.SecretKey;
import java.security.Key;

public class DESedeEncryption extends SymmetricEncryption {

    // DESede key sizes: 112 (effective), 168 (actual) bits
    public static final int[] SUPPORTED_KEY_SIZES = { 112, 168 };

    public DESedeEncryption(String mode, String padding, int keySize) {
        super("DESede", mode, padding, validateKeySize(keySize));
    }

    private static int validateKeySize(int keySize) {
        boolean supported = false;
        for (int size : SUPPORTED_KEY_SIZES) {
            if (keySize == size) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            throw new IllegalArgumentException(
                    "Invalid key size for DESede: " + keySize + ". Supported sizes: 112, 168.");
        }
        return keySize;
    }

    @Override
    public String getName() {
        return "DESede";
    }

    // encrypt and decrypt methods are inherited from SymmetricEncryption
    // generateKey method is inherited from SymmetricEncryption
}