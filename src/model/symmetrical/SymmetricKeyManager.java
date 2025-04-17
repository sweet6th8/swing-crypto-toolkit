package model.symmetrical;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SymmetricKeyManager {
    public static SecretKey generateKey(String algorithm, int size) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(size);
        return keyGen.generateKey();
    }

    public static void saveKey(SecretKey key, String filePath) throws Exception {
        byte[] encoded = key.getEncoded();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(encoded);
        }
    }

    public static String encodeKey(SecretKey key) {
        byte[] encoded = key.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }
}
