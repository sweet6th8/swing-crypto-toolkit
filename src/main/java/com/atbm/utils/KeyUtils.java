package com.atbm.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.util.Base64;

public class KeyUtils {

    public static String generateSymmetricKey(String algorithm, int keySize) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
            keyGen.init(keySize);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Lỗi: Thuật toán không hỗ trợ!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi tạo key!";
        }
    }


    public static String generateAsymmetricKeyPair(String algorithm, int keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm);
            keyPairGen.initialize(keySize);
            KeyPair keyPair = keyPairGen.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            return "Public Key:\n" + publicKey + "\n\nPrivate Key:\n" + privateKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Lỗi: Thuật toán không hỗ trợ!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi tạo key bất đối xứng!";
        }
    }

    public static String generateTraditionalKey(String algorithm) {
        SecureRandom rand = new SecureRandom();
        switch (algorithm) {
            case "Caesar":
                // Trả về 1 số nguyên làm key (ví dụ: từ 1 đến 25)
                int caesarKey = new SecureRandom().nextInt(25) + 1;
                return "Khóa Caesar: " + caesarKey;
            case "Vigenere":
                // Trả về chuỗi chữ cái làm key (ví dụ: ABCDE)
                String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                StringBuilder vigenereKey = new StringBuilder();

                for (int i = 0; i < 5; i++) {
                    vigenereKey.append(characters.charAt(rand.nextInt(characters.length())));
                }
                return "Khóa Vigenere: " + vigenereKey;
            case "Monoalphabetic":
                // Trả về một hoán vị ngẫu nhiên của bảng chữ cái
                char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
                for (int i = 0; i < alphabet.length; i++) {
                    int j = rand.nextInt(alphabet.length);
                    char tmp = alphabet[i];
                    alphabet[i] = alphabet[j];
                    alphabet[j] = tmp;
                }
                return "Khóa Monoalphabetic: " + new String(alphabet);
            default:
                return "Không hỗ trợ thuật toán truyền thống này.";
        }
    }
}
