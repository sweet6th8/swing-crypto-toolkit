package com.atbm.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.util.Base64;

// Class này chứa các phương thức tạo key cho các thuật toán

public class KeyUtils {

    public static String generateSymmetricKey(String algorithm, int keySize) {
        try {
            KeyGenerator keyGen;
            if (algorithm.equalsIgnoreCase("Twofish")) {
                keyGen = KeyGenerator.getInstance(algorithm, "BC");
            } else {
                keyGen = KeyGenerator.getInstance(algorithm);
            }
            keyGen.init(keySize);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
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
                int caesarKey = rand.nextInt(25) + 1;
                return "Khóa Caesar: " + caesarKey;
            case "Vigenere":
                String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                StringBuilder vigenereKey = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    vigenereKey.append(characters.charAt(rand.nextInt(characters.length())));
                }
                return "Khóa Vigenere: " + vigenereKey;
            case "Monoalphabetic":
                char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
                for (int i = 0; i < alphabet.length; i++) {
                    int j = rand.nextInt(alphabet.length);
                    char tmp = alphabet[i];
                    alphabet[i] = alphabet[j];
                    alphabet[j] = tmp;
                }
                return "Khóa Monoalphabetic: " + new String(alphabet);
            case "Affine":
                int[] coprimes = { 1, 3, 5, 7, 9, 11, 15, 17, 19, 21, 23, 25 };
                int a = coprimes[rand.nextInt(coprimes.length)];
                int b = rand.nextInt(26);
                return "Khóa Affine: " + a + "," + b;
            case "Hill":
                while (true) {
                    int[] m = { rand.nextInt(26), rand.nextInt(26), rand.nextInt(26), rand.nextInt(26) };
                    int det = m[0] * m[3] - m[1] * m[2];
                    det = ((det % 26) + 26) % 26;
                    int[] coprimesHill = { 1, 3, 5, 7, 9, 11, 15, 17, 19, 21, 23, 25 };
                    boolean valid = false;
                    for (int x : coprimesHill)
                        if (det == x)
                            valid = true;
                    if (valid)
                        return "Khóa Hill: " + m[0] + "," + m[1] + "," + m[2] + "," + m[3];
                }
            default:
                return "Không hỗ trợ thuật toán truyền thống này.";
        }
    }
}
