package com.atbm.core.encryption.traditional;

import com.atbm.core.encryption.EncryptionAlgorithm;

// Class này là lớp cha cho các thuật toán mã hóa cổ điển
public abstract class TraditionalEncryption implements EncryptionAlgorithm {
    protected String name;

    public TraditionalEncryption(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getSupportedModes() {
        return new String[] { "ECB" };
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "NoPadding" };
    }

    public abstract String encrypt(String plainText, String key);

    public abstract String decrypt(String cipherText, String key);
}