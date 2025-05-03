package com.atbm.core.encryption.asymmetric;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAHybridEncryption {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 256;

    /**
     * Mã hóa dữ liệu sử dụng RSA hybrid encryption
     * 
     * @param data      Dữ liệu cần mã hóa
     * @param publicKey Khóa công khai RSA
     * @return Dữ liệu đã mã hóa theo định dạng: [IV length (4 bytes)][IV][encrypted
     *         AES key][encrypted data]
     * @throws Exception Nếu có lỗi trong quá trình mã hóa
     */
    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        // 1. Tạo khóa AES ngẫu nhiên
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = keyGen.generateKey();

        // 2. Mã hóa dữ liệu bằng AES
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedData = aesCipher.doFinal(data);
        byte[] iv = aesCipher.getIV();

        // 3. Mã hóa khóa AES bằng RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // 4. Kết hợp tất cả dữ liệu
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(intToBytes(iv.length));
        outputStream.write(iv);
        outputStream.write(encryptedAesKey);
        outputStream.write(encryptedData);

        return outputStream.toByteArray();
    }

    /**
     * Giải mã dữ liệu đã được mã hóa bằng RSA hybrid encryption
     * 
     * @param encryptedData Dữ liệu đã mã hóa
     * @param privateKey    Khóa bí mật RSA
     * @return Dữ liệu đã giải mã
     * @throws Exception Nếu có lỗi trong quá trình giải mã
     */
    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedData);

        // 1. Đọc IV length và IV
        byte[] ivLengthBytes = new byte[4];
        inputStream.read(ivLengthBytes);
        int ivLength = bytesToInt(ivLengthBytes);

        byte[] iv = new byte[ivLength];
        inputStream.read(iv);

        // 2. Giải mã khóa AES bằng RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedAesKey = new byte[256]; // Kích thước phụ thuộc vào RSA key size
        inputStream.read(encryptedAesKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

        // 3. Tạo lại khóa AES
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

        // 4. Giải mã dữ liệu bằng AES
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

        byte[] remainingData = inputStream.readAllBytes();
        return aesCipher.doFinal(remainingData);
    }

    // Chuyển đổi int thành byte array
    private static byte[] intToBytes(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) (value >> 24);
        result[1] = (byte) (value >> 16);
        result[2] = (byte) (value >> 8);
        result[3] = (byte) value;
        return result;
    }

    // Chuyển đổi byte array thành int
    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}