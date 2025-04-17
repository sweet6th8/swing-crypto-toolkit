package model.encryption;

import javax.crypto.Cipher;
import java.security.*;

public class RSAEncryption extends AsymmetricEncryption {

    // RSA key sizes: 1024, 2048, 4096 bits
    public static final int[] SUPPORTED_KEY_SIZES = { 1024, 2048, 4096 };

    // Default padding for simplicity, could be configurable
    private String padding = "PKCS1Padding";

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

    @Override
    public String getName() {
        return "RSA";
    }

    /**
     * Encrypts data using the public key.
     */
    public byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/" + padding); // Mode often ECB for raw RSA
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts data using the private key.
     */
    public byte[] decryptWithPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/" + padding); // Mode often ECB for raw RSA
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
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