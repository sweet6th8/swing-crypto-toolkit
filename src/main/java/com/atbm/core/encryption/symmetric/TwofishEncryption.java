package com.atbm.core.encryption.symmetric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

public class TwofishEncryption extends SymmetricEncryption {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public TwofishEncryption() {
        super("Twofish", 256); // Twofish hỗ trợ 128, 192, 256 bit
    }

    @Override
    protected Cipher getCipher(int mode, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("Twofish/CBC/PKCS5Padding", "BC");
        cipher.init(mode, key, iv);
        return cipher;
    }
}