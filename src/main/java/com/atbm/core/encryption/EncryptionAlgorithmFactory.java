package com.atbm.core.encryption;

import com.atbm.core.encryption.symmetric.AESEncryption;
import com.atbm.core.encryption.symmetric.DESedeEncryption;
import com.atbm.core.encryption.symmetric.ChaCha20Poly1305Encryption;
import com.atbm.core.encryption.asymmetric.RSAEncryption;
import com.atbm.core.encryption.traditional.CaesarCipher;
import com.atbm.core.encryption.traditional.VigenereCipher;

public class EncryptionAlgorithmFactory {
    public static EncryptionAlgorithm createAlgorithm(String algorithmName) {
        switch (algorithmName.toLowerCase()) {
            case "aes":
                return new AESEncryption("CBC", "PKCS5Padding", 256);
            case "3des":
                return new DESedeEncryption("CBC", "PKCS5Padding", 168);
            case "chacha20-poly1305":
                return new ChaCha20Poly1305Encryption();
            case "rsa":
                return new RSAEncryption(2048);
            case "caesar":
                return new CaesarCipher();
            case "vigenere":
                return new VigenereCipher();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }

    public static EncryptionAlgorithm createAlgorithmForKeyGen(String algorithmName, int keySize) {
        switch (algorithmName.toLowerCase()) {
            case "aes":
                return new AESEncryption("CBC", "PKCS5Padding", keySize);
            case "desede":
                return new DESedeEncryption("CBC", "PKCS5Padding", keySize);
            case "chacha20-poly1305":
                return new ChaCha20Poly1305Encryption();
            case "rsa":
                return new RSAEncryption(keySize);
            case "caesar":
                return new CaesarCipher();
            case "vigenere":
                return new VigenereCipher();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }

    public static EncryptionAlgorithm createAlgorithmForOperation(String algorithmName, String mode, String padding,
            int keySize) {
        switch (algorithmName.toLowerCase()) {
            case "aes":
                return new AESEncryption(mode, padding, keySize);
            case "desede":
                return new DESedeEncryption(mode, padding, keySize);
            case "chacha20-poly1305":
                return new ChaCha20Poly1305Encryption();
            case "rsa":
                return new RSAEncryption(keySize);
            case "caesar":
                return new CaesarCipher();
            case "vigenere":
                return new VigenereCipher();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }
}