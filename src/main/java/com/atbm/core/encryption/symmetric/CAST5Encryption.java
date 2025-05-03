package com.atbm.core.encryption.symmetric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

public class CAST5Encryption extends SymmetricEncryption {
    public static final int[] SUPPORTED_KEY_SIZES = { 40, 64, 80, 96, 112, 128 };
    public static final int DEFAULT_KEY_SIZE = 128;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CAST5Encryption() {
        super("CAST5", "CBC", "PKCS5Padding", DEFAULT_KEY_SIZE);
    }

    public CAST5Encryption(String mode, String padding, int keySize) {
        super("CAST5", mode, padding, validateKeySize(keySize));
    }

    private static int validateKeySize(int keySize) {
        for (int size : SUPPORTED_KEY_SIZES) {
            if (keySize == size)
                return keySize;
        }
        throw new IllegalArgumentException(
                "Invalid key size for CAST5: " + keySize + ". Supported: 40, 64, 80, 96, 112, 128.");
    }

    @Override
    public String getName() {
        return "CAST5";
    }

    protected Cipher getCipher(int mode, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("CAST5/" + this.mode + "/" + this.padding, "BC");
        cipher.init(mode, key, iv);
        return cipher;
    }
}