package com.atbm.core.key;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyManager {

    // --- Saving Keys ---

    public static void saveKey(Key key, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] keyBytes = key.getEncoded();
            // Encode to Base64 and write
            String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
            fos.write(encodedKey.getBytes());
        }
    }

    public static void saveKeyPair(KeyPair keyPair, String publicKeyPath, String privateKeyPath) throws IOException {
        saveKey(keyPair.getPublic(), publicKeyPath);
        saveKey(keyPair.getPrivate(), privateKeyPath);
    }

    // --- Loading Keys ---

    /**
     * Loads raw key bytes (Base64 decoded) from a file.
     */
    private static byte[] loadRawKeyBytes(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        // Decode Base64
        return Base64.getDecoder().decode(content.toString());
    }

    /**
     * Loads a SecretKey from a file (assuming AES, DESede, etc.).
     *
     * @param filePath  Path to the key file.
     * @param algorithm The algorithm name (e.g., "AES", "DESede") to associate with
     *                  the key.
     * @return The loaded SecretKey.
     */
    public static SecretKey loadSecretKey(String filePath, String algorithm) throws IOException {
        byte[] decodedKey = loadRawKeyBytes(filePath);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }

    /**
     * Loads a PublicKey from a file (assuming RSA, DSA, etc.).
     *
     * @param filePath  Path to the public key file (.pub).
     * @param algorithm The algorithm name (e.g., "RSA", "DSA").
     * @return The loaded PublicKey.
     */
    public static PublicKey loadPublicKey(String filePath, String algorithm)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = loadRawKeyBytes(filePath);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(spec);
    }

    /**
     * Loads a PrivateKey from a file (assuming RSA, DSA, etc.).
     *
     * @param filePath  Path to the private key file (.pri).
     * @param algorithm The algorithm name (e.g., "RSA", "DSA").
     * @return The loaded PrivateKey.
     */
    public static PrivateKey loadPrivateKey(String filePath, String algorithm)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = loadRawKeyBytes(filePath);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Tries to load a Key object based on file extension and algorithm.
     * For asymmetric decryption, it loads the private key.
     * For asymmetric encryption, it loads the public key.
     * For symmetric, it loads the secret key.
     *
     * @param filePath     Path to the key file (.key, .pub, .pri).
     * @param algorithm    The cryptographic algorithm (e.g., "AES", "RSA").
     * @param isEncrypting True if loading the key for encryption, false for
     *                     decryption.
     * @return The loaded Key object (SecretKey, PublicKey, or PrivateKey).
     * @throws Exception If loading fails or key type is inappropriate.
     */
    public static Key loadKeyForOperation(String filePath, String algorithm, boolean isEncrypting) throws Exception {
        String lowerPath = filePath.toLowerCase();
        String upperAlgo = algorithm.toUpperCase();

        if (upperAlgo.equals("AES") || upperAlgo.equals("DESEDE") || upperAlgo.equals("CHACHA20POLY1305")) {
            // Symmetric: Load SecretKey
            if (!lowerPath.endsWith(".key")) {
                throw new IllegalArgumentException("Expected a .key file for symmetric algorithm " + algorithm);
            }
            return loadSecretKey(filePath, algorithm);
        } else if (upperAlgo.equals("RSA")) {
            // Asymmetric: Load Public for Encrypt, Private for Decrypt
            if (isEncrypting) {
                if (!lowerPath.endsWith(".pub")) {
                    throw new IllegalArgumentException("Expected a .pub file for RSA encryption.");
                }
                return loadPublicKey(filePath, algorithm);
            } else {
                if (!lowerPath.endsWith(".pri")) {
                    throw new IllegalArgumentException("Expected a .pri file for RSA decryption.");
                }
                return loadPrivateKey(filePath, algorithm);
            }
        } else if (upperAlgo.equals("CAESAR") || upperAlgo.equals("VIGENERE")) {
            // Traditional: Key file not used in the same way, return null or handle
            // differently
            // Maybe load shift/keyword from the file if that's how you store it?
            return null;
        } else {
            throw new NoSuchAlgorithmException("Unsupported algorithm for key loading: " + algorithm);
        }
    }
}