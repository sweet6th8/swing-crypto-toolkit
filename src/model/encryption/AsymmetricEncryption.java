package model.encryption;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AsymmetricEncryption implements EncryptionAlgorithm {
    protected String algorithm;
    protected int keySize;

    public AsymmetricEncryption(String algorithm, int keySize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        keyGen.initialize(keySize);
        return keyGen.generateKeyPair();
    }

    @Override
    public String[] getSupportedModes() {
        return new String[] { "ECB" };
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "PKCS1Padding", "NoPadding" };
    }
}