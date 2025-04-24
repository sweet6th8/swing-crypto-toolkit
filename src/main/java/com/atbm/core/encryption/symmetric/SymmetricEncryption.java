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
        Cipher cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        if (mode.equals("CBC")) {
            // Use 8 bytes IV for DESede, 16 bytes for others
            int ivLength = algorithm.equals("DESede") ? 8 : 16;
            byte[] iv = new byte[ivLength];
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
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
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        if (mode.equals("CBC")) {
            // Use 8 bytes IV for DESede, 16 bytes for others
            int ivLength = algorithm.equals("DESede") ? 8 : 16;
            byte[] iv = new byte[ivLength];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[encryptedData.length - iv.length];
            System.arraycopy(encryptedData, iv.length, encrypted, 0, encrypted.length);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedData);
        }
    }

    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    @Override
    public String[] getSupportedModes() {
        return new String[] { "ECB", "CBC", "CFB", "OFB" };
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "PKCS5Padding", "NoPadding" };
    }
}