package com.atbm.core.encryption.asymmetric;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

// Đây là class kế thừa AsymmetricEncryption, thực hiện các phương thức cụ thể cho RSA
public class RSAEncryption extends AsymmetricEncryption {

    public static final int[] SUPPORTED_KEY_SIZES = { 1024, 2048, 4096 };

    // Mặc định dùng PKCS1Padding
    private String padding = "PKCS1Padding";

    public RSAEncryption(int keySize) {
        super("RSA", validateKeySize(keySize));
    }

    public RSAEncryption(int keySize, String padding) {
        super("RSA", validateKeySize(keySize));
        this.padding = padding;
    }

    // Kiểm tra hợp lệ của RSA key size
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

    // Phương thức này tính toán ngưỡng kích thước dữ liệu để sử dụng hybrid RSA
    private static int calculateHybridThreshold(int keySize, String padding) {
        // 11 bytes là kích thước của padding PKCS1Padding
        if (padding.equals("PKCS1Padding")) {
            return (keySize / 8) - 11;
            // 42 bytes là kích thước của padding OAEP
        } else if (padding.equals("OAEPWithSHA-1AndMGF1Padding") ||
                padding.equals("OAEPWithSHA-256AndMGF1Padding")) {
            return (keySize / 8) - 42;
        } else if (padding.equals("NoPadding")) {
            return keySize / 8;
        }
        // Mặc định dùng PKCS1Padding
        return (keySize / 8) - 11;
    }

    @Override
    public String getName() {
        return "RSA";
    }

    // Phuownng thức mã hóa sử dụng public key
    public byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        // Hybrid RSA cho dữ liệu lớn
        if (data.length > calculateHybridThreshold(keySize, padding)) {
            return RSAHybridEncryption.encrypt(data, publicKey);
        }

        // RSA trực tiếp cho dữ liệu nhỏ
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/" + padding);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    // Phương thức giải mã sử dụng private key
    public byte[] decryptWithPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        try {
            // thử với RSA trực tiếp
            Cipher cipher = Cipher.getInstance(algorithm + "/ECB/" + padding);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            // nếu thất bại, dùng hybrid RSA
            return RSAHybridEncryption.decrypt(encryptedData, privateKey);
        }
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        if (!(key instanceof PublicKey)) {
            throw new IllegalArgumentException("Encryption requires an RSA Public Key.");
        }
        return encryptWithPublicKey(data, (PublicKey) key);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        if (!(key instanceof PrivateKey)) {
            throw new IllegalArgumentException("Decryption requires an RSA Private Key.");
        }
        return decryptWithPrivateKey(encryptedData, (PrivateKey) key);
    }

    // Trả về các padding hỗ trợ RSA
    @Override
    public String[] getSupportedPaddings() {
        // Common paddings for RSA
        return new String[] { "PKCS1Padding", "OAEPWithSHA-1AndMGF1Padding", "OAEPWithSHA-256AndMGF1Padding",
                "NoPadding" };
    }
}
