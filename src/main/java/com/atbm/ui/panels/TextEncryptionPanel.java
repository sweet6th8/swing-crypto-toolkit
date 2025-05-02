package com.atbm.ui.panels;

import com.atbm.core.encryption.EncryptionAlgorithm;
import com.atbm.core.encryption.EncryptionAlgorithmFactory;
import com.atbm.core.key.KeyManager;
import com.atbm.core.encryption.traditional.CaesarCipher;
import com.atbm.core.encryption.traditional.VigenereCipher;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

public class TextEncryptionPanel extends JPanel {

    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JTextField keyFilePathField;
    private JButton loadKeyButton;
    private JComboBox<String> algorithmComboBox;
    private JComboBox<String> modeComboBox;
    private JComboBox<String> paddingComboBox;
    private JButton encryptButton;
    private JButton decryptButton;
    private JFileChooser fileChooser;

    // Placeholder for loaded key
    private Key loadedKey = null;
    private String loadedTraditionalKey = null;

    public TextEncryptionPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.anchor = GridBagConstraints.CENTER;

        fileChooser = new JFileChooser();

        // --- Input Text Area ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4; // More vertical space
        inputTextArea = new JTextArea();
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder("Nhập văn bản"));
        add(inputScrollPane, gbc);

        // --- Output Text Area ---
        gbc.gridy++;
        outputTextArea = new JTextArea();
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Văn bản đã mã hóa/giải mã"));
        add(outputScrollPane, gbc);

        // --- Key Input ---
        gbc.gridy++;
        gbc.weighty = 0; // Reset weighty
        gbc.fill = GridBagConstraints.HORIZONTAL; // Switch back to horizontal fill
        add(createKeyInputPanel(), gbc);

        // --- Algorithm Selection ---
        gbc.gridy++;
        add(createAlgorithmSelectionPanel(), gbc);

        // --- Action Buttons ---
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.5;
        gbc.weighty = 0;
        encryptButton = new JButton("Mã hóa!");
        add(encryptButton, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.5;
        decryptButton = new JButton("Giải mã!");
        add(decryptButton, gbc);

        setupActionListeners();
        updateModesAndPaddings(); // Initial population
    }

    private JPanel createKeyInputPanel() {
        // Reusing the same structure as FileEncryptionPanel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nhập File Key"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        keyFilePathField = new JTextField(35);
        panel.add(keyFilePathField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        loadKeyButton = new JButton("Load file key!");
        panel.add(loadKeyButton, gbc);

        return panel;
    }

    private JPanel createAlgorithmSelectionPanel() {
        // Reusing the same structure as FileEncryptionPanel
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Lựa chọn thuật toán"));

        JPanel algoPanel = new JPanel(new BorderLayout(5, 0));
        algoPanel.add(new JLabel("Chọn thuật toán:"), BorderLayout.NORTH);
        algorithmComboBox = new JComboBox<>(new String[] {
                "AES",
                "DES",
                "DESede",
                "Blowfish",
                "ChaCha20-Poly1305",
                "RSA",
                "Caesar",
                "Vigenere",
                "Monoalphabetic",
                "Affine",
                "Hill"
        });
        algoPanel.add(algorithmComboBox, BorderLayout.CENTER);
        panel.add(algoPanel);

        JPanel modePanel = new JPanel(new BorderLayout(5, 0));
        modePanel.add(new JLabel("Chọn mode thuật toán:"), BorderLayout.NORTH);
        modeComboBox = new JComboBox<>();
        modePanel.add(modeComboBox, BorderLayout.CENTER);
        panel.add(modePanel);

        JPanel paddingPanel = new JPanel(new BorderLayout(5, 0));
        paddingPanel.add(new JLabel("Chọn padding thuật toán:"), BorderLayout.NORTH);
        paddingComboBox = new JComboBox<>();
        paddingPanel.add(paddingComboBox, BorderLayout.CENTER);
        panel.add(paddingPanel);

        return panel;
    }

    private void setupActionListeners() {
        loadKeyButton.addActionListener(e -> loadKeyFile());
        algorithmComboBox.addActionListener(e -> {
            // Cảnh báo nếu key đã load không còn phù hợp với thuật toán mới
            if (loadedKey != null) {
                String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
                String keyAlgorithm = loadedKey.getAlgorithm();
                if (selectedAlgorithm != null && !selectedAlgorithm.equalsIgnoreCase(keyAlgorithm)) {
                    loadedKey = null;
                    keyFilePathField.setText("");
                    JOptionPane.showMessageDialog(this,
                            "Key đã load không phù hợp với thuật toán mới. Vui lòng load lại file key!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            updateModesAndPaddings();
        });
        encryptButton.addActionListener(e -> performEncryptionDecryption(true));
        decryptButton.addActionListener(e -> performEncryptionDecryption(false));
    }

    private void loadKeyFile() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        if (selectedAlgorithm == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuật toán trước khi load key.", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        File keyDir = new File("./keys");
        if (keyDir.isDirectory()) {
            fileChooser.setCurrentDirectory(keyDir);
        }

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File keyFile = fileChooser.getSelectedFile();
            String keyFilePath = keyFile.getAbsolutePath();
            keyFilePathField.setText(keyFilePath);
            loadedKey = null;
            loadedTraditionalKey = null;

            try {
                boolean forEncryption = true;
                String lowerPath = keyFilePath.toLowerCase();
                String upperAlgo = selectedAlgorithm.toUpperCase();

                if (upperAlgo.equals("RSA")) {
                    if (lowerPath.endsWith(".pri")) {
                        forEncryption = false;
                    } else if (lowerPath.endsWith(".pub")) {
                        forEncryption = true;
                    } else {
                        throw new IllegalArgumentException(
                                "Tệp khóa RSA phải có đuôi .pub (mã hóa) hoặc .pri (giải mã).");
                    }
                } else if (upperAlgo.equals("AES") || upperAlgo.equals("DESEDE")
                        || upperAlgo.equals("CHACHA20-POLY1305")) {
                    if (!lowerPath.endsWith(".key")) {
                        throw new IllegalArgumentException("Tệp khóa " + upperAlgo + " phải có đuôi .key.");
                    }
                } else if (upperAlgo.equals("CAESAR") || upperAlgo.equals("VIGENERE")
                        || upperAlgo.equals("MONOALPHABETIC") || upperAlgo.equals("AFFINE")
                        || upperAlgo.equals("HILL")) {
                    // Đọc key truyền thống từ file
                    String keyText = new String(java.nio.file.Files.readAllBytes(keyFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8);
                    // Loại bỏ tiền tố "Khóa ..." nếu có
                    if (keyText.startsWith("Khóa")) {
                        keyText = keyText.substring(keyText.indexOf(":") + 1).trim();
                    }
                    loadedTraditionalKey = keyText;
                    JOptionPane.showMessageDialog(this, "Load khóa truyền thống thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                loadedKey = KeyManager.loadKeyForOperation(keyFilePath, selectedAlgorithm, forEncryption);

                if (loadedKey != null) {
                    String keyAlgorithm = loadedKey.getAlgorithm();
                    // Debug log để kiểm tra giá trị thực tế
                    System.out.println(
                            "DEBUG: selectedAlgorithm = " + selectedAlgorithm + ", keyAlgorithm = " + keyAlgorithm);
                    if (!keyAlgorithm.equalsIgnoreCase(selectedAlgorithm)) {
                        loadedKey = null;
                        keyFilePathField.setText("");
                        JOptionPane.showMessageDialog(this,
                                "Key bạn chọn không phù hợp với thuật toán đang chọn (" + selectedAlgorithm
                                        + "). Vui lòng chọn đúng file key!\nThuật toán thực tế của key: "
                                        + keyAlgorithm,
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Load khóa thành công!\nThuật toán: " + loadedKey.getAlgorithm()
                                        + "\nĐịnh dạng: " + loadedKey.getFormat(),
                                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

            } catch (Exception ex) {
                loadedKey = null;
                loadedTraditionalKey = null;
                keyFilePathField.setText("");
                JOptionPane.showMessageDialog(this, "Lỗi khi load khóa: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void updateModesAndPaddings() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        modeComboBox.removeAllItems();
        paddingComboBox.removeAllItems();

        if (selectedAlgorithm != null) {
            boolean isBlowfish = selectedAlgorithm.equals("Blowfish");
            boolean isSymmetric = selectedAlgorithm.equals("AES") || selectedAlgorithm.equals("DESede")
                    || selectedAlgorithm.equals("DES");
            boolean isAsymmetric = selectedAlgorithm.equals("RSA");
            boolean isChaCha = selectedAlgorithm.equals("ChaCha20-Poly1305");

            if (isChaCha) {
                // ChaCha20-Poly1305 chỉ hỗ trợ None mode và NoPadding
                modeComboBox.addItem("None");
                paddingComboBox.addItem("NoPadding");
                modeComboBox.setSelectedItem("None");
                paddingComboBox.setSelectedItem("NoPadding");
                modeComboBox.setEnabled(false);
                paddingComboBox.setEnabled(false);
            } else if (isBlowfish || isSymmetric) {
                modeComboBox.addItem("ECB");
                modeComboBox.addItem("CBC");
                paddingComboBox.addItem("PKCS5Padding");
                paddingComboBox.addItem("NoPadding");
                modeComboBox.setEnabled(true);
                paddingComboBox.setEnabled(true);
                modeComboBox.setSelectedItem("CBC");
                paddingComboBox.setSelectedItem("PKCS5Padding");
            } else if (isAsymmetric) {
                modeComboBox.addItem("ECB");
                modeComboBox.setEnabled(false);
                paddingComboBox.addItem("PKCS1Padding");
                paddingComboBox.addItem("OAEPWithSHA-1AndMGF1Padding");
                paddingComboBox.addItem("OAEPWithSHA-256AndMGF1Padding");
                paddingComboBox.addItem("NoPadding");
                paddingComboBox.setEnabled(true);
            } else {
                modeComboBox.addItem("None");
                paddingComboBox.addItem("NoPadding");
                modeComboBox.setSelectedItem("None");
                paddingComboBox.setSelectedItem("NoPadding");
                modeComboBox.setEnabled(false);
                paddingComboBox.setEnabled(false);
            }
        } else {
            modeComboBox.setEnabled(false);
            paddingComboBox.setEnabled(false);
        }
    }

    private void performEncryptionDecryption(boolean encrypt) {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        String mode = (String) modeComboBox.getSelectedItem();
        String padding = (String) paddingComboBox.getSelectedItem();
        String inputText = inputTextArea.getText();
        String upperAlgo = algorithm != null ? algorithm.toUpperCase() : "";

        // Xử lý cho thuật toán truyền thống
        if (upperAlgo.equals("CAESAR") || upperAlgo.equals("VIGENERE")
                || upperAlgo.equals("MONOALPHABETIC") || upperAlgo.equals("AFFINE") || upperAlgo.equals("HILL")) {
            if (loadedTraditionalKey == null || loadedTraditionalKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng load file key.", "Lỗi Mã hóa",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            EncryptionAlgorithm algoInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(algorithm, mode,
                    padding, 0);
            if (algoInstance instanceof com.atbm.core.encryption.traditional.TraditionalEncryption) {
                String result;
                if (encrypt) {
                    result = ((com.atbm.core.encryption.traditional.TraditionalEncryption) algoInstance)
                            .encrypt(inputText, loadedTraditionalKey);
                } else {
                    result = ((com.atbm.core.encryption.traditional.TraditionalEncryption) algoInstance)
                            .decrypt(inputText, loadedTraditionalKey);
                }
                outputTextArea.setText(result);
            } else {
                JOptionPane.showMessageDialog(this, "Thuật toán không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // --- Input Validation ---
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập văn bản đầu vào.",
                    "Lỗi " + (encrypt ? "Mã hóa" : "Giải mã"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (loadedKey == null && loadedTraditionalKey == null) {
            if (algorithm != null && (algorithm.equalsIgnoreCase("Caesar") || algorithm.equalsIgnoreCase("Vigenere"))) {
                // Allow proceeding for traditional
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng load file key.",
                        "Lỗi " + (encrypt ? "Mã hóa" : "Giải mã"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (algorithm == null || (modeComboBox.isEnabled() && mode == null)
                || (paddingComboBox.isEnabled() && padding == null)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đầy đủ thuật toán" + (modeComboBox.isEnabled() ? ", mode" : "")
                            + (paddingComboBox.isEnabled() ? " và padding." : "."),
                    "Lỗi " + (encrypt ? "Mã hóa" : "Giải mã"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Key Type Validation (Similar to FileEncryptionPanel)
        if (upperAlgo.equals("RSA")) {
            if (encrypt && !(loadedKey instanceof java.security.PublicKey)) {
                JOptionPane.showMessageDialog(this, "Mã hóa RSA yêu cầu khóa Công khai (.pub).", "Lỗi Key",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!encrypt && !(loadedKey instanceof java.security.PrivateKey)) {
                JOptionPane.showMessageDialog(this, "Giải mã RSA yêu cầu khóa Bí mật (.pri).", "Lỗi Key",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (upperAlgo.equals("AES")) { // Add other symmetric checks
            if (!(loadedKey instanceof javax.crypto.SecretKey)) {
                JOptionPane.showMessageDialog(this, "Thao tác " + upperAlgo + " yêu cầu khóa Bí mật (.key).", "Lỗi Key",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // --- Perform Operation ---
        try {
            // --- Determine actual key size from loaded key ---
            int actualKeySize = 0;
            if (loadedKey instanceof java.security.interfaces.RSAKey) {
                // Get nominal key size for RSA keys
                actualKeySize = ((java.security.interfaces.RSAKey) loadedKey).getModulus().bitLength();
            } else if (loadedKey instanceof javax.crypto.SecretKey) {
                String keyAlgorithm = loadedKey.getAlgorithm();
                if (keyAlgorithm.equals("DESede")) {
                    // For DESede, check actual key length
                    byte[] encodedKey = loadedKey.getEncoded();
                    if (encodedKey != null) {
                        if (encodedKey.length == 24) { // 24 bytes = 192 bits raw = 168 bits effective
                            actualKeySize = 168;
                        } else if (encodedKey.length == 16) { // 16 bytes = 128 bits raw = 112 bits effective
                            actualKeySize = 112;
                        } else {
                            throw new IllegalArgumentException(
                                    "Invalid DESede key length: " + (encodedKey.length * 8)
                                            + " bits. Supported sizes: 112, 168 bits.");
                        }
                    }
                } else {
                    // For other symmetric keys, encoding length * 8 is correct
                    byte[] encodedKey = loadedKey.getEncoded();
                    if (encodedKey != null) {
                        actualKeySize = encodedKey.length * 8;
                    }
                }
            }
            // For traditional ciphers, actualKeySize remains 0 or irrelevant
            // --- End determine actual key size ---

            // Get algorithm instance using determined key size
            EncryptionAlgorithm algoInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(algorithm, mode,
                    padding, actualKeySize);

            byte[] inputBytes;
            byte[] outputBytes;
            String outputText;

            long startTime = System.currentTimeMillis();

            if (loadedKey == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng load file key.",
                        "Lỗi " + (encrypt ? "Mã hóa" : "Giải mã"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Gọi mã hóa hiện đại với loadedKey
            if (encrypt) {
                inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
                outputBytes = algoInstance.encrypt(inputBytes, loadedKey);
                // Encode result to Base64 for display/storage in text format
                outputText = Base64.getEncoder().encodeToString(outputBytes);
            } else {
                // Assume input text is Base64 encoded ciphertext
                try {
                    inputBytes = Base64.getDecoder().decode(inputText);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi giải mã Base64: Dữ liệu đầu vào không hợp lệ.",
                            "Lỗi Giải mã", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                outputBytes = algoInstance.decrypt(inputBytes, loadedKey);
                // Convert decrypted bytes back to String
                outputText = new String(outputBytes, StandardCharsets.UTF_8);
            }

            outputTextArea.setText(outputText);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            JOptionPane.showMessageDialog(this,
                    String.format("%s văn bản thành công! (Thời gian: %d ms)", (encrypt ? "Mã hóa" : "Giải mã"),
                            duration),
                    "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            outputTextArea.setText("Lỗi khi " + (encrypt ? "Mã hóa" : "Giải mã") + ": " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi " + (encrypt ? "Mã hóa" : "Giải mã") + ": " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Main method for testing this panel independently
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("TextEncryptionPanel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new TextEncryptionPanel());
        frame.setSize(600, 700); // Adjust size for text areas
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Thêm hàm tiện ích xác định thuật toán truyền thống
    private boolean isTraditionalAlgorithm(String algorithm) {
        if (algorithm == null)
            return false;
        String upper = algorithm.toUpperCase();
        return upper.equals("CAESAR") || upper.equals("VIGENERE")
                || upper.equals("MONOALPHABETIC") || upper.equals("AFFINE") || upper.equals("HILL");
    }
}