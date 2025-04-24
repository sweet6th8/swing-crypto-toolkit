package com.atbm.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtils {
    public static File ensureKeyDirectory() {
        String path = System.getProperty("user.home") + "/keys/";
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

    /**
     * Reads all bytes from a file.
     *
     * @param filePath The path to the file.
     * @return A byte array containing the file's contents.
     * @throws IOException If an I/O error occurs reading from the file.
     */
    public static byte[] readFileBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    /**
     * Writes a byte array to a file, overwriting the file if it exists.
     *
     * @param filePath The path to the file.
     * @param data     The byte array to write.
     * @throws IOException If an I/O error occurs writing to the file.
     */
    public static void writeFileBytes(String filePath, byte[] data) throws IOException {
        Files.write(Paths.get(filePath), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Writes a byte array to a file, creating the file if it does not exist,
     * or appending to it if it does.
     *
     * @param filePath The path to the file.
     * @param data     The byte array to write.
     * @throws IOException If an I/O error occurs writing to the file.
     */
    public static void appendFileBytes(String filePath, byte[] data) throws IOException {
        Files.write(Paths.get(filePath), data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
