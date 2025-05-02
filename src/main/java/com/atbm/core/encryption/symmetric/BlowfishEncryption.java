package com.atbm.core.encryption.symmetric;

public class BlowfishEncryption extends SymmetricEncryption {
    // Blowfish supports key sizes from 32 to 448 bits
    public static final int[] SUPPORTED_KEY_SIZES = { 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416,
            448 };
    public static final int DEFAULT_KEY_SIZE = 128;

    public BlowfishEncryption(String mode, String padding) {
        super("Blowfish", mode, padding, DEFAULT_KEY_SIZE);
    }

    public BlowfishEncryption(String mode, String padding, int keySize) {
        super("Blowfish", mode, padding, validateKeySize(keySize));
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
                    "Invalid key size for Blowfish: " + keySize
                            + ". Supported sizes: 32-448 bits in 32-bit increments.");
        }
        return keySize;
    }

    @Override
    public String getName() {
        return "Blowfish";
    }
}