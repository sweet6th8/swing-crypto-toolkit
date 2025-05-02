package com.atbm.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.security.Key;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.function.Consumer;

// Class này chứa các phương thức để đảm bảo tồn tại thư mục keys và đọc/ghi file

public class FileUtils {
    // Kích thước buffer mặc định là 8MB cho streaming
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024 * 1024; // 8MB

    // Giới hạn kích thước file mặc định là 1GB
    private static final long DEFAULT_MAX_FILE_SIZE = 1024L * 1024 * 1024; // 1GB

    // Đảm bảo tồn tại thư mục keys, nếu không tồn tại thì tạo mới
    public static File ensureKeyDirectory() {
        String path = System.getProperty("user.home") + "/keys/";
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

    // Đọc file và trả về byte array, với kiểm tra kích thước
    public static byte[] readFileBytes(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();

        if (fileSize > DEFAULT_MAX_FILE_SIZE) {
            throw new IOException("File quá lớn: " + formatFileSize(fileSize) +
                    ". Giới hạn tối đa: " + formatFileSize(DEFAULT_MAX_FILE_SIZE));
        }

        if (fileSize <= DEFAULT_BUFFER_SIZE) {
            // Đọc file nhỏ một lần
            return Files.readAllBytes(Paths.get(filePath));
        } else {
            // Đọc file lớn theo chunks
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[(int) fileSize];
                int bytesRead;
                int offset = 0;
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

                while ((bytesRead = fis.read(buffer)) != -1) {
                    System.arraycopy(buffer, 0, data, offset, bytesRead);
                    offset += bytesRead;
                }
                return data;
            }
        }
    }

    // Ghi byte array vào file, với kiểm tra kích thước
    public static void writeFileBytes(String filePath, byte[] data) throws IOException {
        if (data.length > DEFAULT_MAX_FILE_SIZE) {
            throw new IOException("Dữ liệu quá lớn: " + formatFileSize(data.length) +
                    ". Giới hạn tối đa: " + formatFileSize(DEFAULT_MAX_FILE_SIZE));
        }

        if (data.length <= DEFAULT_BUFFER_SIZE) {
            // Ghi file nhỏ một lần
            Files.write(Paths.get(filePath), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            // Ghi file lớn theo chunks
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                int offset = 0;
                while (offset < data.length) {
                    int length = Math.min(DEFAULT_BUFFER_SIZE, data.length - offset);
                    fos.write(data, offset, length);
                    offset += length;
                }
            }
        }
    }

    // Ghi byte array vào file, thêm vào cuối
    public static void appendFileBytes(String filePath, byte[] data) throws IOException {
        if (data.length > DEFAULT_MAX_FILE_SIZE) {
            throw new IOException("Dữ liệu quá lớn: " + formatFileSize(data.length) +
                    ". Giới hạn tối đa: " + formatFileSize(DEFAULT_MAX_FILE_SIZE));
        }

        if (data.length <= DEFAULT_BUFFER_SIZE) {
            // Ghi file nhỏ một lần
            Files.write(Paths.get(filePath), data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else {
            // Ghi file lớn theo chunks
            try (FileOutputStream fos = new FileOutputStream(filePath, true)) {
                int offset = 0;
                while (offset < data.length) {
                    int length = Math.min(DEFAULT_BUFFER_SIZE, data.length - offset);
                    fos.write(data, offset, length);
                    offset += length;
                }
            }
        }
    }

    // Định dạng kích thước file để hiển thị
    public static String formatFileSize(long size) {
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double fileSize = size;

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    // Phương thức mã hóa file lớn theo stream
    public static void encryptFile(String inputFile, String outputFile, Cipher cipher, boolean isChaCha20Poly1305,
            Consumer<Double> progressCallback, String mode) throws IOException {
        byte[] iv = cipher.getIV(); // Lấy IV thực tế từ Cipher (nếu có)

        try (FileInputStream fis = new FileInputStream(inputFile);
                FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Chỉ ghi IV nếu mode là CBC hoặc các mode cần IV
            if (iv != null && mode != null && mode.equalsIgnoreCase("CBC")) {
                fos.write(iv);
            }

            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;
                long fileSize = new File(inputFile).length();

                while ((bytesRead = fis.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (progressCallback != null) {
                        progressCallback.accept((double) totalBytesRead / fileSize);
                    }
                }
            }
        }
    }

    // Overload cũ để tương thích
    public static void encryptFile(String inputFile, String outputFile, Cipher cipher, boolean isChaCha20Poly1305,
            Consumer<Double> progressCallback) throws IOException {
        encryptFile(inputFile, outputFile, cipher, isChaCha20Poly1305, progressCallback, null);
    }

    // Phương thức giải mã file lớn theo stream
    public static void decryptFile(String inputFile, String outputFile, Cipher cipher, boolean isChaCha20Poly1305,
            Consumer<Double> progressCallback) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            if (!isChaCha20Poly1305) {
                byte[] iv = new byte[cipher.getBlockSize()];
                int bytesRead = fis.read(iv);
                if (bytesRead != iv.length) {
                    throw new IOException("File không hợp lệ: không thể đọc IV");
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile);
                    CipherInputStream cis = new CipherInputStream(fis, cipher)) {

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;
                long fileSize = new File(inputFile).length();

                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (progressCallback != null) {
                        progressCallback.accept((double) totalBytesRead / fileSize);
                    }
                }
            }
        }
    }

    // Phương thức cập nhật tiến trình
    private static void updateProgress(long current, long total) {
        double progress = (double) current / total * 100;
        // TODO: Implement progress callback
        // System.out.printf("Progress: %.2f%%\n", progress);
    }

    // Đọc toàn bộ file nhỏ
    public static byte[] readAllBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    // Ghi toàn bộ file nhỏ
    public static void writeAllBytes(String filePath, byte[] data) throws IOException {
        Files.write(Paths.get(filePath), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Keep the old methods for backward compatibility
    public static void encryptFile(String inputFile, String outputFile, Cipher cipher, boolean isChaCha20Poly1305)
            throws IOException {
        encryptFile(inputFile, outputFile, cipher, isChaCha20Poly1305, null);
    }

    public static void decryptFile(String inputFile, String outputFile, Cipher cipher, boolean isChaCha20Poly1305)
            throws IOException {
        decryptFile(inputFile, outputFile, cipher, isChaCha20Poly1305, null);
    }
}
