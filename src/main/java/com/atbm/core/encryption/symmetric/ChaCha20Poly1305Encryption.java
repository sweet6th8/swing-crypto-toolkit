package com.atbm.core.encryption.symmetric;

// Class này mã hóa và giải mã dữ liệu sử dụng ChaCha20-Poly1305
public class ChaCha20Poly1305Encryption extends SymmetricEncryption {

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

}