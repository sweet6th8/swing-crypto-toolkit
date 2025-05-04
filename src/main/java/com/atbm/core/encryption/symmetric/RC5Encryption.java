package com.atbm.core.encryption.symmetric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

// Class này mã hóa và giải mã dữ liệu sử dụng RC5
public class RC5Encryption extends SymmetricEncryption {
    public static final int[] SUPPORTED_KEY_SIZES = { 64, 128, 192, 256 };
    public static final int DEFAULT_KEY_SIZE = 128;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public RC5Encryption() {
        super("RC5", "CBC", "PKCS5Padding", DEFAULT_KEY_SIZE);
    }

    public RC5Encryption(String mode, String padding, int keySize) {
        super("RC5", mode, padding, validateKeySize(keySize));
    }

    private static int validateKeySize(int keySize) {
        for (int size : SUPPORTED_KEY_SIZES) {
            if (keySize == size)
                return keySize;
        }
        throw new IllegalArgumentException("Invalid key size for RC5: " + keySize + ". Supported: 64, 128, 192, 256.");
    }

    @Override
    public String getName() {
        return "RC5";
    }

    protected Cipher getCipher(int mode, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("RC5/" + this.mode + "/" + this.padding, "BC");
        cipher.init(mode, key, iv);
        return cipher;
    }
}