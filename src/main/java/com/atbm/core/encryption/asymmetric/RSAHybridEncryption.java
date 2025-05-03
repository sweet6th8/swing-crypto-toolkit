package com.atbm.core.encryption.asymmetric;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAHybridEncryption {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer size

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
     * @throws GeneralSecurityException Nếu có lỗi trong quá trình mã hóa
     */
    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws GeneralSecurityException {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("Data cannot be empty");
        }

        try {
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
            try {
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
            } catch (IOException e) {
                throw new GeneralSecurityException("Error writing encrypted data: " + e.getMessage(), e);
            }

            return outputStream.toByteArray();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new GeneralSecurityException("Error initializing encryption algorithm: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new GeneralSecurityException("Invalid key: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new GeneralSecurityException("Error during encryption: " + e.getMessage(), e);
        }
    }

    /**
     * Giải mã dữ liệu đã được mã hóa bằng RSA hybrid encryption
     * 
     * @param encryptedData Dữ liệu đã mã hóa
     * @param privateKey    Khóa bí mật RSA
     * @return Dữ liệu đã giải mã
     * @throws GeneralSecurityException Nếu có lỗi trong quá trình giải mã
     */
    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws GeneralSecurityException {
        if (encryptedData == null) {
            throw new IllegalArgumentException("Encrypted data cannot be null");
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("Private key cannot be null");
        }
        if (encryptedData.length < MAGIC_NUMBER_LENGTH + 8) { // Minimum size: magic + IV length + AES key length
            throw new IllegalArgumentException("Encrypted data is too short");
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedData);

            // 1. Kiểm tra magic number
            byte[] magic = new byte[MAGIC_NUMBER_LENGTH];
            if (readBytes(inputStream, magic) != MAGIC_NUMBER_LENGTH || !isValidMagicNumber(magic)) {
                throw new GeneralSecurityException("Invalid data format: not a hybrid encrypted file");
            }

            // 2. Đọc IV length và IV
            byte[] ivLengthBytes = new byte[4];
            if (readBytes(inputStream, ivLengthBytes) != 4) {
                throw new GeneralSecurityException("Invalid data format: cannot read IV length");
            }
            int ivLength = bytesToInt(ivLengthBytes);
            if (ivLength <= 0 || ivLength > 16) { // AES IV is always 16 bytes
                throw new GeneralSecurityException("Invalid IV length: " + ivLength);
            }

            byte[] iv = new byte[ivLength];
            if (readBytes(inputStream, iv) != ivLength) {
                throw new GeneralSecurityException("Invalid data format: cannot read IV");
            }

            // 3. Đọc độ dài và dữ liệu khóa AES đã mã hóa
            byte[] aesKeyLengthBytes = new byte[4];
            if (readBytes(inputStream, aesKeyLengthBytes) != 4) {
                throw new GeneralSecurityException("Invalid data format: cannot read AES key length");
            }
            int aesKeyLength = bytesToInt(aesKeyLengthBytes);
            if (aesKeyLength <= 0 || aesKeyLength > 512) { // Reasonable limit for RSA encrypted AES key
                throw new GeneralSecurityException("Invalid AES key length: " + aesKeyLength);
            }

            byte[] encryptedAesKey = new byte[aesKeyLength];
            if (readBytes(inputStream, encryptedAesKey) != aesKeyLength) {
                throw new GeneralSecurityException("Invalid data format: cannot read AES key");
            }

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
            if (remainingData.length == 0) {
                throw new GeneralSecurityException("No encrypted data found");
            }
            return aesCipher.doFinal(remainingData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new GeneralSecurityException("Error initializing decryption algorithm: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new GeneralSecurityException("Invalid key: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new GeneralSecurityException("Error during decryption: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new GeneralSecurityException("Error reading encrypted data: " + e.getMessage(), e);
        }
    }

    /**
     * Mã hóa file sử dụng RSA hybrid encryption với streaming
     * 
     * @param inputFile  File cần mã hóa
     * @param outputFile File kết quả
     * @param publicKey  Khóa công khai RSA
     * @throws GeneralSecurityException Nếu có lỗi trong quá trình mã hóa
     * @throws IOException              Nếu có lỗi khi đọc/ghi file
     */
    public static void encryptFile(File inputFile, File outputFile, PublicKey publicKey)
            throws GeneralSecurityException, IOException {
        if (inputFile == null || outputFile == null) {
            throw new IllegalArgumentException("Input and output files cannot be null");
        }
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Input file does not exist: " + inputFile.getPath());
        }
        if (inputFile.length() == 0) {
            throw new IllegalArgumentException("Input file is empty");
        }

        try {
            // 1. Tạo khóa AES ngẫu nhiên
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE);
            SecretKey aesKey = keyGen.generateKey();

            // 2. Mã hóa khóa AES bằng RSA
            Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

            // 3. Khởi tạo AES cipher
            Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] iv = aesCipher.getIV();

            // 4. Ghi header và dữ liệu đã mã hóa
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                // Ghi magic number
                fos.write(MAGIC_NUMBER);
                // Ghi độ dài IV
                fos.write(intToBytes(iv.length));
                // Ghi IV
                fos.write(iv);
                // Ghi độ dài khóa AES đã mã hóa
                fos.write(intToBytes(encryptedAesKey.length));
                // Ghi khóa AES đã mã hóa
                fos.write(encryptedAesKey);

                // 5. Mã hóa và ghi dữ liệu theo từng phần
                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        byte[] encryptedChunk = aesCipher.update(buffer, 0, bytesRead);
                        if (encryptedChunk != null) {
                            fos.write(encryptedChunk);
                        }
                    }
                    // Ghi phần cuối cùng
                    byte[] finalChunk = aesCipher.doFinal();
                    if (finalChunk != null) {
                        fos.write(finalChunk);
                    }
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new GeneralSecurityException("Error initializing encryption algorithm: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new GeneralSecurityException("Invalid key: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new GeneralSecurityException("Error during encryption: " + e.getMessage(), e);
        }
    }

    /**
     * Giải mã file đã được mã hóa bằng RSA hybrid encryption với streaming
     * 
     * @param inputFile  File cần giải mã
     * @param outputFile File kết quả
     * @param privateKey Khóa bí mật RSA
     * @throws GeneralSecurityException Nếu có lỗi trong quá trình giải mã
     * @throws IOException              Nếu có lỗi khi đọc/ghi file
     */
    public static void decryptFile(File inputFile, File outputFile, PrivateKey privateKey)
            throws GeneralSecurityException, IOException {
        if (inputFile == null || outputFile == null) {
            throw new IllegalArgumentException("Input and output files cannot be null");
        }
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Input file does not exist: " + inputFile.getPath());
        }
        if (inputFile.length() < MAGIC_NUMBER_LENGTH + 8) {
            throw new IllegalArgumentException("Input file is too short");
        }

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            // 1. Đọc và kiểm tra magic number
            byte[] magic = new byte[MAGIC_NUMBER_LENGTH];
            if (readBytes(fis, magic) != MAGIC_NUMBER_LENGTH || !isValidMagicNumber(magic)) {
                throw new GeneralSecurityException("Invalid data format: not a hybrid encrypted file");
            }

            // 2. Đọc IV length và IV
            byte[] ivLengthBytes = new byte[4];
            if (readBytes(fis, ivLengthBytes) != 4) {
                throw new GeneralSecurityException("Invalid data format: cannot read IV length");
            }
            int ivLength = bytesToInt(ivLengthBytes);
            if (ivLength <= 0 || ivLength > 16) {
                throw new GeneralSecurityException("Invalid IV length: " + ivLength);
            }

            byte[] iv = new byte[ivLength];
            if (readBytes(fis, iv) != ivLength) {
                throw new GeneralSecurityException("Invalid data format: cannot read IV");
            }

            // 3. Đọc độ dài và dữ liệu khóa AES đã mã hóa
            byte[] aesKeyLengthBytes = new byte[4];
            if (readBytes(fis, aesKeyLengthBytes) != 4) {
                throw new GeneralSecurityException("Invalid data format: cannot read AES key length");
            }
            int aesKeyLength = bytesToInt(aesKeyLengthBytes);
            if (aesKeyLength <= 0 || aesKeyLength > 512) {
                throw new GeneralSecurityException("Invalid AES key length: " + aesKeyLength);
            }

            byte[] encryptedAesKey = new byte[aesKeyLength];
            if (readBytes(fis, encryptedAesKey) != aesKeyLength) {
                throw new GeneralSecurityException("Invalid data format: cannot read AES key");
            }

            // 4. Giải mã khóa AES bằng RSA
            Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

            // 5. Tạo lại khóa AES
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

            // 6. Giải mã dữ liệu theo từng phần
            Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] decryptedChunk = aesCipher.update(buffer, 0, bytesRead);
                    if (decryptedChunk != null) {
                        fos.write(decryptedChunk);
                    }
                }
                // Giải mã phần cuối cùng
                byte[] finalChunk = aesCipher.doFinal();
                if (finalChunk != null) {
                    fos.write(finalChunk);
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new GeneralSecurityException("Error initializing decryption algorithm: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new GeneralSecurityException("Invalid key: " + e.getMessage(), e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new GeneralSecurityException("Error during decryption: " + e.getMessage(), e);
        }
    }

    // Helper method để đọc bytes từ input stream
    private static int readBytes(InputStream inputStream, byte[] buffer) throws IOException {
        int totalRead = 0;
        int read;
        while (totalRead < buffer.length
                && (read = inputStream.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
            totalRead += read;
        }
        return totalRead;
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
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Byte array length must be 4");
        }
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}