package com.atbm.core.encryption.asymmetric;

import javax.crypto.Cipher;
import java.security.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import com.atbm.core.encryption.EncryptionAlgorithm;

public class RSAEncryption extends AsymmetricEncryption {

    // RSA key sizes: 1024, 2048, 4096 bits
    public static final int[] SUPPORTED_KEY_SIZES = { 1024, 2048, 4096 };

    // Default padding for simplicity, could be configurable
    private String padding = "PKCS1Padding";

    // Ngưỡng kích thước dữ liệu để sử dụng hybrid encryption (bytes)
    // Giá trị này được tính toán dựa trên kích thước khóa RSA và padding
    // Với PKCS1Padding, kích thước tối đa có thể mã hóa = keySize/8 - 11 bytes
    private static final int HYBRID_THRESHOLD = 100; // Có thể điều chỉnh tùy theo nhu cầu

    public RSAEncryption(int keySize) {
        super("RSA", validateKeySize(keySize));
    }

    public RSAEncryption(int keySize, String padding) {
        super("RSA", validateKeySize(keySize));
        // TODO: Validate supported paddings for RSA
        this.padding = padding;
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
                    "Invalid key size for RSA: " + keySize + ". Supported sizes: 1024, 2048, 4096.");
        }
        return keySize;
    }

    /**
     * Tính toán ngưỡng kích thước dữ liệu để sử dụng hybrid encryption
     * Dựa trên kích thước khóa RSA và padding scheme
     * 
     * @param keySize Kích thước khóa RSA (bits)
     * @param padding Padding scheme
     * @return Ngưỡng kích thước dữ liệu (bytes)
     */
    private static int calculateHybridThreshold(int keySize, String padding) {
        // Với PKCS1Padding, kích thước tối đa có thể mã hóa = keySize/8 - 11 bytes
        if (padding.equals("PKCS1Padding")) {
            return (keySize / 8) - 11;
        }
        // Với OAEP, kích thước tối đa có thể mã hóa = keySize/8 - 42 bytes
        else if (padding.equals("OAEPWithSHA-1AndMGF1Padding") ||
                padding.equals("OAEPWithSHA-256AndMGF1Padding")) {
            return (keySize / 8) - 42;
        }
        // Với NoPadding, kích thước tối đa có thể mã hóa = keySize/8 bytes
        else if (padding.equals("NoPadding")) {
            return keySize / 8;
        }
        // Mặc định sử dụng PKCS1Padding
        return (keySize / 8) - 11;
    }

    @Override
    public String getName() {
        return "RSA";
    }

    /**
     * Encrypts data using the public key.
     * Uses hybrid encryption for large data.
     */
    public byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        // Sử dụng hybrid encryption cho dữ liệu lớn
        if (data.length > calculateHybridThreshold(keySize, padding)) {
            return RSAHybridEncryption.encrypt(data, publicKey);
        }

        // Sử dụng RSA trực tiếp cho dữ liệu nhỏ
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/" + padding);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts data using the private key.
     * Handles both direct RSA and hybrid encryption.
     */
    public byte[] decryptWithPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        try {
            // Thử giải mã bằng RSA trực tiếp trước
            Cipher cipher = Cipher.getInstance(algorithm + "/ECB/" + padding);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            // Nếu thất bại, thử giải mã bằng hybrid encryption
            return RSAHybridEncryption.decrypt(encryptedData, privateKey);
        }
    }

    // --- Interface Methods (Adaptation) ---

    /**
     * Encrypts using the provided key. Assumes it's a PublicKey.
     * 
     * @throws IllegalArgumentException if the key is not a PublicKey.
     */
    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        if (!(key instanceof PublicKey)) {
            throw new IllegalArgumentException("Encryption requires an RSA Public Key.");
        }
        return encryptWithPublicKey(data, (PublicKey) key);
    }

    /**
     * Decrypts using the provided key. Assumes it's a PrivateKey.
     * 
     * @throws IllegalArgumentException if the key is not a PrivateKey.
     */
    @Override
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        if (!(key instanceof PrivateKey)) {
            throw new IllegalArgumentException("Decryption requires an RSA Private Key.");
        }
        return decryptWithPrivateKey(encryptedData, (PrivateKey) key);
    }

    @Override
    public String[] getSupportedPaddings() {
        // Common paddings for RSA
        return new String[] { "PKCS1Padding", "OAEPWithSHA-1AndMGF1Padding", "OAEPWithSHA-256AndMGF1Padding",
                "NoPadding" };
    }

    // generateKeyPair is inherited from AsymmetricEncryption
}