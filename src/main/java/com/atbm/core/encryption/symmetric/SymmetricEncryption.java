package com.atbm.core.encryption.symmetric;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.SecureRandom;
import com.atbm.core.encryption.EncryptionAlgorithm;

public abstract class SymmetricEncryption implements EncryptionAlgorithm {
    protected String algorithm;
    protected String mode;
    protected String padding;
    protected int keySize;

    // Standard Nonce length for ChaCha20-Poly1305
    private static final int CHACHA_NONCE_LENGTH_BYTES = 12;
    // Standard Tag length for ChaCha20-Poly1305 and GCM
    private static final int AEAD_TAG_LENGTH_BITS = 128;

    public SymmetricEncryption(String algorithm, String mode, String padding, int keySize) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.padding = padding;
        this.keySize = keySize;
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        String transformation;
        if (algorithm.equals("ChaCha20")) { // Check for ChaCha20
            transformation = "ChaCha20-Poly1305";
        } else {
            transformation = algorithm + "/" + mode + "/" + padding;
        }
        Cipher cipher = Cipher.getInstance(transformation);

        if (algorithm.equals("ChaCha20")) {
            // ChaCha20-Poly1305: generate nonce, encrypt, prepend nonce
            byte[] nonce = new byte[CHACHA_NONCE_LENGTH_BYTES];
            new SecureRandom().nextBytes(nonce);
            // Using IvParameterSpec instead of GCMParameterSpec due to provider constraint
            IvParameterSpec parameterSpec = new IvParameterSpec(nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec); // Use IvParameterSpec
            byte[] encryptedData = cipher.doFinal(data);
            // Prepend nonce to the encrypted data
            byte[] result = new byte[nonce.length + encryptedData.length];
            System.arraycopy(nonce, 0, result, 0, nonce.length);
            System.arraycopy(encryptedData, 0, result, nonce.length, encryptedData.length);
            return result;
        } else if (mode.equals("CBC")) {
            // Existing CBC IV handling
            int ivLength = algorithm.equals("DESede") ? 8 : 16;
            byte[] iv = new byte[ivLength];
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(data);
            // Prepend IV
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return result;
        } else {
            // Other modes (ECB, CFB, OFB) - No IV prepended (OFB/CFB might need IV handling
            // too, simplified here)
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedDataWithPrefix, Key key) throws Exception {
        String transformation;
        if (algorithm.equals("ChaCha20")) { // Check for ChaCha20
            transformation = "ChaCha20-Poly1305";
        } else {
            transformation = algorithm + "/" + mode + "/" + padding;
        }
        Cipher cipher = Cipher.getInstance(transformation);

        if (algorithm.equals("ChaCha20")) {
            // ChaCha20-Poly1305: extract nonce, init with nonce, decrypt
            if (encryptedDataWithPrefix == null || encryptedDataWithPrefix.length < CHACHA_NONCE_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted data length for ChaCha20-Poly1305");
            }
            byte[] nonce = new byte[CHACHA_NONCE_LENGTH_BYTES];
            System.arraycopy(encryptedDataWithPrefix, 0, nonce, 0, nonce.length);
            byte[] actualEncryptedData = new byte[encryptedDataWithPrefix.length - nonce.length];
            System.arraycopy(encryptedDataWithPrefix, nonce.length, actualEncryptedData, 0, actualEncryptedData.length);

            // Using IvParameterSpec instead of GCMParameterSpec due to provider constraint
            IvParameterSpec parameterSpec = new IvParameterSpec(nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec); // Use IvParameterSpec
            return cipher.doFinal(actualEncryptedData);
        } else if (mode.equals("CBC")) {
            // Existing CBC IV handling
            int ivLength = algorithm.equals("DESede") ? 8 : 16;
            if (encryptedDataWithPrefix == null || encryptedDataWithPrefix.length < ivLength) {
                throw new IllegalArgumentException("Invalid encrypted data length for CBC mode");
            }
            byte[] iv = new byte[ivLength];
            System.arraycopy(encryptedDataWithPrefix, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[encryptedDataWithPrefix.length - iv.length];
            System.arraycopy(encryptedDataWithPrefix, iv.length, encrypted, 0, encrypted.length);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv)); // Init with IV!
            return cipher.doFinal(encrypted);
        } else {
            // Other modes (ECB, CFB, OFB) - No IV extracted
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedDataWithPrefix);
        }
    }

    public SecretKey generateKey() throws Exception {
        // Use the correct algorithm name for KeyGenerator
        KeyGenerator keyGen;
        if (algorithm.equals("ChaCha20")) {
            keyGen = KeyGenerator.getInstance("ChaCha20"); // JCA uses "ChaCha20" for key gen
        } else {
            keyGen = KeyGenerator.getInstance(algorithm);
        }
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    @Override
    public String[] getSupportedModes() {
        // ChaCha20 doesn't really have modes in the traditional sense
        if (algorithm.equals("ChaCha20")) {
            return new String[] { "None" };
        }
        // Return standard modes for others
        return new String[] { "ECB", "CBC", "CFB", "OFB" };
    }

    @Override
    public String[] getSupportedPaddings() {
        // ChaCha20 doesn't use padding
        if (algorithm.equals("ChaCha20")) {
            return new String[] { "NoPadding" };
        }
        // Return standard paddings for others (adjust based on algo if needed)
        if (algorithm.equals("DESede")) {
            return new String[] { "PKCS5Padding", "NoPadding" };
        }
        return new String[] { "PKCS5Padding", "NoPadding" /* Add others like ISO10126Padding if supported/needed */ };
    }
}