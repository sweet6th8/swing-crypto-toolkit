package com.atbm.core.encryption.symmetric;

import javax.crypto.SecretKey;
import java.security.Key;

public class ChaCha20Poly1305Encryption extends SymmetricEncryption {

    // ChaCha20-Poly1305 key size is fixed at 256 bits
    public static final int KEY_SIZE = 256;

    public ChaCha20Poly1305Encryption() {
        super("ChaCha20-Poly1305", "None", "NoPadding", KEY_SIZE);
    }

    @Override
    public String getName() {
        return "ChaCha20-Poly1305";
    }

    @Override
    public String[] getSupportedModes() {
        return new String[] { "None" };
    }

    @Override
    public String[] getSupportedPaddings() {
        return new String[] { "NoPadding" };
    }

    // encrypt and decrypt methods are inherited from SymmetricEncryption
    // generateKey method is inherited from SymmetricEncryption
}