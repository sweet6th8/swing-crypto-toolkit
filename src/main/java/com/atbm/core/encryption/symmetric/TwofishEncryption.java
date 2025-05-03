package com.atbm.core.encryption.symmetric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

public class TwofishEncryption extends SymmetricEncryption {
    public static final int[] SUPPORTED_KEY_SIZES = { 128, 192, 256 };

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public TwofishEncryption() {
        super("Twofish", "CBC", "PKCS5Padding", 256);
    }

    public TwofishEncryption(String mode, String padding, int keySize) {
        super("Twofish", mode, padding, validateKeySize(keySize));
    }

    private static int validateKeySize(int keySize) {
        for (int size : SUPPORTED_KEY_SIZES) {
            if (keySize == size)
                return keySize;
        }
        throw new IllegalArgumentException("Invalid key size for Twofish: " + keySize + ". Supported: 128, 192, 256.");
    }

    @Override
    public String getName() {
        return "Twofish";
    }

    protected Cipher getCipher(int mode, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("Twofish/" + this.mode + "/" + this.padding, "BC");
        cipher.init(mode, key, iv);
        return cipher;
    }
}