package com.atbm.core.encryption.symmetric;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.SecureRandom;
import com.atbm.core.encryption.EncryptionAlgorithm;

public abstract class SymmetricEncryption implements EncryptionAlgorithm {
    protected String algorithm;
    protected String mode;
    protected String padding;
    protected int keySize;

    public SymmetricEncryption(String algorithm, String mode, String padding, int keySize) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.padding = padding;
        this.keySize = keySize;
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        String transformation;
        if (algorithm.equals("ChaCha20-Poly1305")) {
            transformation = "ChaCha20-Poly1305";
        } else {
            transformation = algorithm + "/" + mode + "/" + padding;
        }
        Cipher cipher = Cipher.getInstance(transformation);

        // Handle manual padding if NoPadding is selected
        byte[] dataToEncrypt = data;
        if (padding.equals("NoPadding") && !algorithm.equals("ChaCha20-Poly1305")) {
            int blockSize = cipher.getBlockSize();
            int paddingLength = blockSize - (data.length % blockSize);
            if (paddingLength > 0 && paddingLength < blockSize) {
                dataToEncrypt = new byte[data.length + paddingLength];
                System.arraycopy(data, 0, dataToEncrypt, 0, data.length);
                // Fill remaining bytes with zeros
                for (int i = data.length; i < dataToEncrypt.length; i++) {
                    dataToEncrypt[i] = 0;
                }
            }
        }

        if (algorithm.equals("ChaCha20-Poly1305")) {
            // ChaCha20-Poly1305: generate nonce, encrypt, prepend nonce
            byte[] nonce = new byte[12]; // 12 bytes nonce for ChaCha20-Poly1305
            new SecureRandom().nextBytes(nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(nonce));
            byte[] encryptedData = cipher.doFinal(dataToEncrypt);

            // Combine nonce and encrypted data
            byte[] result = new byte[nonce.length + encryptedData.length];
            System.arraycopy(nonce, 0, result, 0, nonce.length);
            System.arraycopy(encryptedData, 0, result, nonce.length, encryptedData.length);
            return result;
        } else if (mode.equals("CBC")) {
            // Handle IV for CBC mode
            int ivLength = algorithm.equals("DESede") ? 8 : 16;
            byte[] iv = new byte[ivLength];
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(dataToEncrypt);
            // Prepend IV
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return result;
        } else {
            // ECB mode - No IV needed
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(dataToEncrypt);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedDataWithPrefix, Key key) throws Exception {
        String transformation;
        if (algorithm.equals("ChaCha20-Poly1305")) {
            transformation = "ChaCha20-Poly1305";
        } else {
            transformation = algorithm + "/" + mode + "/" + padding;
        }
        Cipher cipher = Cipher.getInstance(transformation);

        if (algorithm.equals("ChaCha20-Poly1305")) {
            // Extract nonce and encrypted data
            if (encryptedDataWithPrefix.length < 12) {
                throw new IllegalArgumentException("Invalid encrypted data length for ChaCha20-Poly1305");
            }
            byte[] nonce = new byte[12];
            System.arraycopy(encryptedDataWithPrefix, 0, nonce, 0, 12);

            byte[] encryptedData = new byte[encryptedDataWithPrefix.length - 12];
            System.arraycopy(encryptedDataWithPrefix, 12, encryptedData, 0, encryptedData.length);

            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(nonce));
            return cipher.doFinal(encryptedData);
        } else if (mode.equals("CBC")) {
            // Handle IV for CBC mode
            int ivLength = algorithm.equals("DESede") ? 8 : 16;
            if (encryptedDataWithPrefix == null || encryptedDataWithPrefix.length < ivLength) {
                throw new IllegalArgumentException("Invalid encrypted data length for CBC mode");
            }
            byte[] iv = new byte[ivLength];
            System.arraycopy(encryptedDataWithPrefix, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[encryptedDataWithPrefix.length - iv.length];
            System.arraycopy(encryptedDataWithPrefix, iv.length, encrypted, 0, encrypted.length);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] decryptedData = cipher.doFinal(encrypted);

            // Remove padding zeros if NoPadding was used
            if (padding.equals("NoPadding")) {
                int i = decryptedData.length - 1;
                while (i >= 0 && decryptedData[i] == 0) {
                    i--;
                }
                if (i < decryptedData.length - 1) {
                    byte[] trimmed = new byte[i + 1];
                    System.arraycopy(decryptedData, 0, trimmed, 0, i + 1);
                    return trimmed;
                }
            }
            return decryptedData;
        } else {
            // ECB mode - No IV needed
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedData = cipher.doFinal(encryptedDataWithPrefix);

            // Remove padding zeros if NoPadding was used
            if (padding.equals("NoPadding")) {
                int i = decryptedData.length - 1;
                while (i >= 0 && decryptedData[i] == 0) {
                    i--;
                }
                if (i < decryptedData.length - 1) {
                    byte[] trimmed = new byte[i + 1];
                    System.arraycopy(decryptedData, 0, trimmed, 0, i + 1);
                    return trimmed;
                }
            }
            return decryptedData;
        }
    }

    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    @Override
    public String[] getSupportedModes() {
        return new String[] { "ECB", "CBC" };
    }

    @Override
    public String[] getSupportedPaddings() {
        if (algorithm.equals("DESede")) {
            return new String[] { "PKCS5Padding", "NoPadding" };
        }
        return new String[] { "PKCS5Padding", "NoPadding" };
    }
}