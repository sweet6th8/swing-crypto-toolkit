package com.atbm.core.key;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

// Class này chứa các phương thức lưu và tải key
public class KeyManager {

    // Lưu key vào file
    public static void saveKey(Key key, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] keyBytes = key.getEncoded();
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            fos.write(encodedKey.getBytes());
        }
    }

    public static void saveKeyPair(KeyPair keyPair, String publicKeyPath, String privateKeyPath) throws IOException {
        saveKey(keyPair.getPublic(), publicKeyPath);
        saveKey(keyPair.getPrivate(), privateKeyPath);
    }

    // Tải key từ file
    private static byte[] loadRawKeyBytes(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return Base64.getDecoder().decode(content.toString());
    }

    // Tải key bí mật từ file
    public static SecretKey loadSecretKey(String filePath, String algorithm) throws IOException {
        byte[] decodedKey = loadRawKeyBytes(filePath);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }

    // Tải public key
    public static PublicKey loadPublicKey(String filePath, String algorithm)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = loadRawKeyBytes(filePath);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(spec);
    }

    // tải private key
    public static PrivateKey loadPrivateKey(String filePath, String algorithm)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = loadRawKeyBytes(filePath);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePrivate(spec);
    }

    // load key cho từng loại
    public static Key loadKeyForOperation(String filePath, String algorithm, boolean isEncrypting) throws Exception {
        String lowerPath = filePath.toLowerCase();
        String upperAlgo = algorithm.toUpperCase();

        if (upperAlgo.equals("AES") || upperAlgo.equals("DES") || upperAlgo.equals("DESEDE")
                || upperAlgo.equals("CHACHA20-POLY1305") || upperAlgo.equals("BLOWFISH")
                || upperAlgo.equals("TWOFISH") || upperAlgo.equals("CAMELLIA") || upperAlgo.equals("CAST5")
                || upperAlgo.equals("RC5")) {
            if (!lowerPath.endsWith(".key")) {
                throw new IllegalArgumentException("File key không đúng định dạng cho thuật toán " + algorithm);
            }
            return loadSecretKey(filePath, algorithm);
        } else if (upperAlgo.equals("RSA")) {
            if (isEncrypting) {
                if (!lowerPath.endsWith(".pub")) {
                    throw new IllegalArgumentException("Cần file .pub để mã hóa RSA");
                }
                return loadPublicKey(filePath, algorithm);
            } else {
                if (!lowerPath.endsWith(".pri")) {
                    throw new IllegalArgumentException("Cần file .pri để giải mã RSA");
                }
                return loadPrivateKey(filePath, algorithm);
            }
        } else if (upperAlgo.equals("CAESAR") || upperAlgo.equals("VIGENERE")
                || upperAlgo.equals("MONOALPHABETIC") || upperAlgo.equals("AFFINE") || upperAlgo.equals("HILL")) {
            return null;
        } else {
            throw new NoSuchAlgorithmException("Không hỗ trợ thuật toán: " + algorithm);
        }
    }

    public static SecretKey generateTwofishKey(int keySize) throws Exception {
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("Twofish", "BC");
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    public static SecretKey generateCamelliaKey(int keySize) throws Exception {
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("Camellia", "BC");
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    public static SecretKey generateCAST5Key(int keySize) throws Exception {
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("CAST5", "BC");
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    public static SecretKey generateRC5Key(int keySize) throws Exception {
        javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("RC5", "BC");
        keyGen.init(keySize);
        return keyGen.generateKey();
    }
}