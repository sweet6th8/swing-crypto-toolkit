package com.atbm.core.encryption;

import com.atbm.core.encryption.symmetric.AESEncryption;
import com.atbm.core.encryption.symmetric.DESedeEncryption;
import com.atbm.core.encryption.symmetric.DESEncryption;
import com.atbm.core.encryption.symmetric.ChaCha20Poly1305Encryption;
import com.atbm.core.encryption.asymmetric.RSAEncryption;
import com.atbm.core.encryption.traditional.CaesarCipher;
import com.atbm.core.encryption.traditional.VigenereCipher;
import com.atbm.core.encryption.traditional.MonoalphabeticCipher;
import com.atbm.core.encryption.traditional.AffineCipher;
import com.atbm.core.encryption.traditional.HillCipher;

public class EncryptionAlgorithmFactory {
    public static EncryptionAlgorithm createAlgorithm(String algorithmName) {
        switch (algorithmName.toLowerCase()) {
            case "aes":
                return new AESEncryption("CBC", "PKCS5Padding", 256);
            case "des":
                return new DESEncryption("CBC", "PKCS5Padding", DESEncryption.KEY_SIZE);
            case "3des":
            case "desede":
                return new DESedeEncryption("CBC", "PKCS5Padding", 168);
            case "chacha20-poly1305":
                return new ChaCha20Poly1305Encryption();
            case "rsa":
                return new RSAEncryption(2048);
            case "caesar":
                return new CaesarCipher();
            case "vigenere":
                return new VigenereCipher();
            case "monoalphabetic":
                return new MonoalphabeticCipher();
            case "affine":
                return new AffineCipher();
            case "hill":
                return new HillCipher();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }

    public static EncryptionAlgorithm createAlgorithmForKeyGen(String algorithmName, int keySize) {
        switch (algorithmName.toLowerCase()) {
            case "aes":
                return new AESEncryption("CBC", "PKCS5Padding", keySize);
            case "des":
                return new DESEncryption("CBC", "PKCS5Padding", DESEncryption.KEY_SIZE);
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
            case "monoalphabetic":
                return new MonoalphabeticCipher();
            case "affine":
                return new AffineCipher();
            case "hill":
                return new HillCipher();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }

    public static EncryptionAlgorithm createAlgorithmForOperation(String algorithmName, String mode, String padding,
            int keySize) {
        switch (algorithmName.toLowerCase()) {
            case "aes":
                return new AESEncryption(mode, padding, keySize);
            case "des":
                return new DESEncryption(mode, padding, DESEncryption.KEY_SIZE);
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
            case "monoalphabetic":
                return new MonoalphabeticCipher();
            case "affine":
                return new AffineCipher();
            case "hill":
                return new HillCipher();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }
}