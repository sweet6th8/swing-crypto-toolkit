package com.atbm.core.encryption.symmetric;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.SecureRandom;

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

    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        String transformation = "Blowfish/" + mode + "/" + padding;
        Cipher cipher = Cipher.getInstance(transformation);

        if (mode.equals("CBC")) {
            byte[] iv = new byte[8]; // Blowfish block size is 64 bits (8 bytes)
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            // Prepend IV to encrypted data
            byte[] encrypted = cipher.doFinal(data);
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return result;
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedDataWithPrefix, Key key) throws Exception {
        String transformation = "Blowfish/" + mode + "/" + padding;
        Cipher cipher = Cipher.getInstance(transformation);

        if (mode.equals("CBC")) {
            // Extract IV and encrypted data
            if (encryptedDataWithPrefix.length < 8) {
                throw new IllegalArgumentException("Invalid encrypted data length for Blowfish CBC");
            }
            byte[] iv = new byte[8];
            System.arraycopy(encryptedDataWithPrefix, 0, iv, 0, 8);

            byte[] encryptedData = new byte[encryptedDataWithPrefix.length - 8];
            System.arraycopy(encryptedDataWithPrefix, 8, encryptedData, 0, encryptedData.length);

            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return cipher.doFinal(encryptedData);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedDataWithPrefix);
        }
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