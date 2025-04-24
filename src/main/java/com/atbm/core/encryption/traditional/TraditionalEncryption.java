package com.atbm.core.encryption.traditional;

import java.security.Key;
import com.atbm.core.encryption.EncryptionAlgorithm;

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
}