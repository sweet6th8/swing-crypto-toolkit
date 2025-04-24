package com.atbm.core.encryption;

import java.security.Key;

public interface EncryptionAlgorithm {
    byte[] encrypt(byte[] data, Key key) throws Exception;

    byte[] decrypt(byte[] encryptedData, Key key) throws Exception;

    String getName();

    String[] getSupportedModes();

    String[] getSupportedPaddings();
}