package com.atbm.core.encryption.symmetric;

// Class này mã hóa và giải mã dữ liệu sử dụng AES
public class AESEncryption extends SymmetricEncryption {

    public static final int[] SUPPORTED_KEY_SIZES = { 128, 192, 256 };

    public AESEncryption(String mode, String padding, int keySize) {
        super("AES", mode, padding, validateKeySize(keySize));
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
                    "Invalid key size for AES: " + keySize + ". Supported sizes: 128, 192, 256.");
        }
        return keySize;
    }

    @Override
    public String getName() {
        return "AES";
    }

}