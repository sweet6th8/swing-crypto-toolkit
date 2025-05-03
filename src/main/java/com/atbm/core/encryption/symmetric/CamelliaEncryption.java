package com.atbm.core.encryption.symmetric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

public class CamelliaEncryption extends SymmetricEncryption {
    public static final int[] SUPPORTED_KEY_SIZES = { 128, 192, 256 };

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CamelliaEncryption() {
        super("Camellia", "CBC", "PKCS5Padding", 256);
    }

    public CamelliaEncryption(String mode, String padding, int keySize) {
        super("Camellia", mode, padding, validateKeySize(keySize));
    }

    private static int validateKeySize(int keySize) {
        for (int size : SUPPORTED_KEY_SIZES) {
            if (keySize == size)
                return keySize;
        }
        throw new IllegalArgumentException("Invalid key size for Camellia: " + keySize + ". Supported: 128, 192, 256.");
    }

    @Override
    public String getName() {
        return "Camellia";
    }

    protected Cipher getCipher(int mode, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("Camellia/" + this.mode + "/" + this.padding, "BC");
        cipher.init(mode, key, iv);
        return cipher;
    }
}