package com.atbm.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

// Class này chứa các phương thức để đảm bảo tồn tại thư mục keys và đọc/ghi file

public class FileUtils {
    // Đảm bảo tồn tại thư mục keys, nếu không tồn tại thì tạo mới
    public static File ensureKeyDirectory() {
        String path = System.getProperty("user.home") + "/keys/";
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

    // Đọc file và trả về byte array
    public static byte[] readFileBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    // Ghi byte array vào file, tạo file nếu không tồn tại, ghi đè nếu tồn tại
    public static void writeFileBytes(String filePath, byte[] data) throws IOException {
        Files.write(Paths.get(filePath), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Ghi byte array vào file, tạo file nếu không tồn tại, thêm vào nếu tồn tại
    public static void appendFileBytes(String filePath, byte[] data) throws IOException {
        Files.write(Paths.get(filePath), data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
