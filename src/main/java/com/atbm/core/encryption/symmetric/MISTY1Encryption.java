package com.atbm.core.encryption.symmetric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

public class MISTY1Encryption extends SymmetricEncryption {
    public static final int KEY_SIZE = 128;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public MISTY1Encryption() {
        super("MISTY1", "CBC", "PKCS5Padding", KEY_SIZE);
    }

    public MISTY1Encryption(String mode, String padding, int keySize) {
        super("MISTY1", mode, padding, validateKeySize(keySize));
    }

    private static int validateKeySize(int keySize) {
        if (keySize == KEY_SIZE) {
            return keySize;
        }
        throw new IllegalArgumentException("Invalid key size for MISTY1: " + keySize + ". Supported: 128.");
    }

    @Override
    public String getName() {
        return "MISTY1";
    }

    protected Cipher getCipher(int mode, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("MISTY1/" + this.mode + "/" + this.padding, "BC");
        cipher.init(mode, key, iv);
        return cipher;
    }
}