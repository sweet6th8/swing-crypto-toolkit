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
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    // Magic number để xác định đây là file hybrid
    private static final byte[] MAGIC_NUMBER = "HYBRID".getBytes();
    private static final int MAGIC_NUMBER_LENGTH = MAGIC_NUMBER.length;

    /**
     * Mã hóa dữ liệu sử dụng RSA hybrid encryption
     * 
     * @param data      Dữ liệu cần mã hóa
     * @param publicKey Khóa công khai RSA
     * @return Dữ liệu đã mã hóa theo định dạng: [MAGIC_NUMBER][IV length (4
     *         bytes)][IV][encrypted
     *         AES key length (4 bytes)][encrypted AES key][encrypted data]
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
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // 4. Kết hợp tất cả dữ liệu
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Ghi magic number
        outputStream.write(MAGIC_NUMBER);
        // Ghi độ dài IV
        outputStream.write(intToBytes(iv.length));
        // Ghi IV
        outputStream.write(iv);
        // Ghi độ dài khóa AES đã mã hóa
        outputStream.write(intToBytes(encryptedAesKey.length));
        // Ghi khóa AES đã mã hóa
        outputStream.write(encryptedAesKey);
        // Ghi dữ liệu đã mã hóa
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

        // 1. Kiểm tra magic number
        byte[] magic = new byte[MAGIC_NUMBER_LENGTH];
        inputStream.read(magic);
        if (!isValidMagicNumber(magic)) {
            throw new Exception("Dữ liệu không hợp lệ: không phải file hybrid");
        }

        // 2. Đọc IV length và IV
        byte[] ivLengthBytes = new byte[4];
        inputStream.read(ivLengthBytes);
        int ivLength = bytesToInt(ivLengthBytes);

        byte[] iv = new byte[ivLength];
        inputStream.read(iv);

        // 3. Đọc độ dài và dữ liệu khóa AES đã mã hóa
        byte[] aesKeyLengthBytes = new byte[4];
        inputStream.read(aesKeyLengthBytes);
        int aesKeyLength = bytesToInt(aesKeyLengthBytes);

        byte[] encryptedAesKey = new byte[aesKeyLength];
        inputStream.read(encryptedAesKey);

        // 4. Giải mã khóa AES bằng RSA
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

        // 5. Tạo lại khóa AES
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

        // 6. Giải mã dữ liệu bằng AES
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

        byte[] remainingData = inputStream.readAllBytes();
        return aesCipher.doFinal(remainingData);
    }

    // Kiểm tra magic number có hợp lệ không
    private static boolean isValidMagicNumber(byte[] magic) {
        if (magic.length != MAGIC_NUMBER_LENGTH) {
            return false;
        }
        for (int i = 0; i < MAGIC_NUMBER_LENGTH; i++) {
            if (magic[i] != MAGIC_NUMBER[i]) {
                return false;
            }
        }
        return true;
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