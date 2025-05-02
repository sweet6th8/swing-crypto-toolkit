package com.atbm.ui.panels;

import com.atbm.utils.FileUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.security.Key;
import com.atbm.core.encryption.EncryptionAlgorithm;
import com.atbm.core.encryption.EncryptionAlgorithmFactory;
import com.atbm.core.key.KeyManager;
import com.atbm.core.encryption.symmetric.AESEncryption;
import com.atbm.core.encryption.symmetric.DESedeEncryption;
import com.atbm.core.encryption.symmetric.ChaCha20Poly1305Encryption;
import com.atbm.core.encryption.asymmetric.RSAEncryption;
import com.atbm.core.encryption.traditional.CaesarCipher;
import com.atbm.core.encryption.traditional.VigenereCipher;

public class FileEncryptionPanel extends JPanel implements DropTargetListener {

    private JTextField inputFilePathField;
    private JButton browseInputButton;
    private JTextField keyFilePathField;
    private JButton loadKeyButton;
    private JComboBox<String> algorithmComboBox;
    private JComboBox<String> modeComboBox;
    private JComboBox<String> paddingComboBox;
    private JTextField outputFilePathField;
    private JButton browseOutputButton;
    private JButton encryptButton;
    private JButton decryptButton;
    private JFileChooser fileChooser;
    private JLabel dragDropLabel;

    // Placeholder for loaded key and selected file
    private Key loadedKey = null;
    private File selectedInputFile = null;

    public FileEncryptionPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        fileChooser = new JFileChooser();

        // --- Drag and Drop Area ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4; // Give it some vertical space
        gbc.fill = GridBagConstraints.BOTH;
        JPanel dropPanel = createDropPanel();
        add(dropPanel, gbc);

        // --- Key Input ---
        gbc.gridy++;
        gbc.weighty = 0; // Reset weighty
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createKeyInputPanel(), gbc);

        // --- Algorithm Selection ---
        gbc.gridy++;
        add(createAlgorithmSelectionPanel(), gbc);

        // --- Output Selection ---
        gbc.gridy++;
        add(createOutputPanel(), gbc);

        // --- Action Buttons ---
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.5;
        encryptButton = new JButton("Mã hóa!");
        add(encryptButton, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.5;
        decryptButton = new JButton("Giải mã!");
        add(decryptButton, gbc);

        // Make panel a drop target
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
        new DropTarget(dragDropLabel, DnDConstants.ACTION_COPY_OR_MOVE, this, true); // Make label a drop target too

        setupActionListeners();
        updateModesAndPaddings(); // Initial population
    }

    private JPanel createDropPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Chọn File Đầu Vào"));

        dragDropLabel = new JLabel(
                "<html><div style='text-align: center;'>Kéo và thả file vào đây!<br/> hoặc</div></html>",
                SwingConstants.CENTER);
        dragDropLabel.setFont(dragDropLabel.getFont().deriveFont(Font.ITALIC, 16f));
        dragDropLabel.setPreferredSize(new Dimension(300, 100)); // Give it a size
        dragDropLabel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 5, 5));
        panel.add(dragDropLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        browseInputButton = new JButton("Chọn File...");
        inputFilePathField = new JTextField(30);
        inputFilePathField.setEditable(false);
        bottomPanel.add(inputFilePathField);
        bottomPanel.add(browseInputButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createKeyInputPanel() {
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
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0)); // Use GridLayout for equal spacing
        panel.setBorder(BorderFactory.createTitledBorder("Lựa chọn thuật toán"));

        // Algorithm
        JPanel algoPanel = new JPanel(new BorderLayout(5, 0));
        algoPanel.add(new JLabel("Chọn thuật toán:"), BorderLayout.NORTH);
        algorithmComboBox = new JComboBox<>(new String[] {
                "AES",
                "DES",
                "DESede",
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

        // Mode
        JPanel modePanel = new JPanel(new BorderLayout(5, 0));
        modePanel.add(new JLabel("Chọn mode thuật toán:"), BorderLayout.NORTH);
        modeComboBox = new JComboBox<>();
        modePanel.add(modeComboBox, BorderLayout.CENTER);
        panel.add(modePanel);

        // Padding
        JPanel paddingPanel = new JPanel(new BorderLayout(5, 0));
        paddingPanel.add(new JLabel("Chọn padding thuật toán:"), BorderLayout.NORTH);
        paddingComboBox = new JComboBox<>();
        paddingPanel.add(paddingComboBox, BorderLayout.CENTER);
        panel.add(paddingPanel);

        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Xuất kết quả"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        outputFilePathField = new JTextField(35);
        panel.add(outputFilePathField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        browseOutputButton = new JButton("Lưu File...");
        panel.add(browseOutputButton, gbc);

        return panel;
    }

    private void setupActionListeners() {
        browseInputButton.addActionListener(e -> browseInputFile());
        loadKeyButton.addActionListener(e -> loadKeyFile());
        browseOutputButton.addActionListener(e -> browseOutputFile());

        algorithmComboBox.addActionListener(e -> updateModesAndPaddings());

        encryptButton.addActionListener(e -> performEncryptionDecryption(true));
        decryptButton.addActionListener(e -> performEncryptionDecryption(false));
    }

    private void browseInputFile() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            handleSelectedFile(fileChooser.getSelectedFile());
        }
    }

    private void handleSelectedFile(File file) {
        selectedInputFile = file;
        inputFilePathField.setText(file.getAbsolutePath());
        // Suggest output filename based on input
        String inputName = file.getName();
        String outputName = inputName.contains(".") ? inputName.substring(0, inputName.lastIndexOf('.')) + ".enc"
                : inputName + ".enc";
        outputFilePathField.setText(file.getParent() + File.separator + outputName);
        dragDropLabel.setText(
                "<html><div style='text-align: center;'>File đã chọn:<br/>" + file.getName() + "</div></html>");
        dragDropLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2)); // Indicate success
    }

    private void loadKeyFile() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        if (selectedAlgorithm == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuật toán trước khi load key.", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Determine if loading for encryption or decryption (needed for asymmetric)
        // For simplicity, assume loading a key is always for *potential* use in both.
        // We will rely on the file extension (.pub/.pri) for asymmetric.
        // A better approach might involve separate load buttons or context.

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // Suggest key directory
        File keyDir = new File("./keys");
        if (keyDir.isDirectory()) {
            fileChooser.setCurrentDirectory(keyDir);
        }

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File keyFile = fileChooser.getSelectedFile();
            String keyFilePath = keyFile.getAbsolutePath();
            keyFilePathField.setText(keyFilePath);
            loadedKey = null; // Reset previous key

            try {
                // Determine if we are likely encrypting or decrypting based on filename? Risky.
                // Let's determine based on expected file types for the algorithm.
                boolean forEncryption = true; // Default assumption, refined below
                String lowerPath = keyFilePath.toLowerCase();
                String upperAlgo = selectedAlgorithm.toUpperCase();

                if (upperAlgo.equals("RSA")) {
                    if (lowerPath.endsWith(".pri")) {
                        // If loading private key, likely for decryption
                        forEncryption = false;
                    } else if (lowerPath.endsWith(".pub")) {
                        // If loading public key, likely for encryption
                        forEncryption = true;
                    } else {
                        throw new IllegalArgumentException(
                                "Tệp khóa RSA phải có đuôi .pub (mã hóa) hoặc .pri (giải mã).");
                    }
                } else if (upperAlgo.equals("AES")) { // Add other symmetric algos here
                    if (!lowerPath.endsWith(".key")) {
                        throw new IllegalArgumentException("Tệp khóa " + upperAlgo + " phải có đuôi .key.");
                    }
                    // Doesn't matter for symmetric
                } else if (upperAlgo.equals("CAESAR") || upperAlgo.equals("VIGENERE")) {
                    // Key file might not be applicable or have custom format
                    JOptionPane.showMessageDialog(this, "Load khóa từ file không áp dụng cho " + selectedAlgorithm,
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return; // Or load shift/keyword from file if implemented
                }

                // Use the refined KeyManager method
                loadedKey = KeyManager.loadKeyForOperation(keyFilePath, selectedAlgorithm, forEncryption);

                if (loadedKey != null) {
                    JOptionPane.showMessageDialog(this, "Load khóa thành công!\nThuật toán: " + loadedKey.getAlgorithm()
                            + "\nĐịnh dạng: " + loadedKey.getFormat(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Should be handled by exceptions or specific messages now
                }

            } catch (Exception ex) {
                loadedKey = null;
                keyFilePathField.setText(""); // Clear field on error
                JOptionPane.showMessageDialog(this, "Lỗi khi load khóa: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void browseOutputFile() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // Set suggested name if input file is selected
        if (selectedInputFile != null) {
            String outputName = outputFilePathField.getText();
            if (!outputName.isEmpty()) {
                fileChooser.setSelectedFile(new File(outputName));
            } else {
                String inputName = selectedInputFile.getName();
                String suggestedName = inputName.contains(".")
                        ? inputName.substring(0, inputName.lastIndexOf('.')) + ".out"
                        : inputName + ".out";
                fileChooser.setSelectedFile(new File(selectedInputFile.getParent() + File.separator + suggestedName));
            }
        }
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            outputFilePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void updateModesAndPaddings() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        modeComboBox.removeAllItems();
        paddingComboBox.removeAllItems();

        if (selectedAlgorithm != null) {
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
            } else if (isSymmetric) {
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
        String operation = encrypt ? "Mã hóa" : "Giải mã";
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        String mode = (String) modeComboBox.getSelectedItem();
        String padding = (String) paddingComboBox.getSelectedItem();
        String outputFilePath = outputFilePathField.getText().trim();

        // --- Input Validation ---
        if (selectedInputFile == null || !selectedInputFile.exists()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file đầu vào hợp lệ.", "Lỗi " + operation,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (loadedKey == null) {
            // Check if it's a traditional cipher that doesn't need a loaded key file
            if (algorithm != null && (algorithm.equalsIgnoreCase("Caesar") || algorithm.equalsIgnoreCase("Vigenere"))) {
                // Allow proceeding, key is handled differently (e.g., via input dialog later)
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng load file key.", "Lỗi " + operation,
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (outputFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đường dẫn file đầu ra.", "Lỗi " + operation,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (algorithm == null || (modeComboBox.isEnabled() && mode == null)
                || (paddingComboBox.isEnabled() && padding == null)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đầy đủ thuật toán" + (modeComboBox.isEnabled() ? ", mode" : "")
                            + (paddingComboBox.isEnabled() ? " và padding." : "."),
                    "Lỗi " + operation, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Key Type Validation (Crucial for Asymmetric)
        String upperAlgo = algorithm.toUpperCase();
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
            // Read input file
            byte[] inputData = FileUtils.readFileBytes(selectedInputFile.getAbsolutePath());
            byte[] outputData;

            // --- Determine actual key size from loaded key ---
            int actualKeySize = 0;
            if (loadedKey instanceof java.security.interfaces.RSAKey) {
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

            long startTime = System.currentTimeMillis();

            // Special handling for RSA with large files
            if (algorithm.equals("RSA")) {
                if (encrypt) {
                    // Generate a random AES key for the file
                    javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
                    keyGen.init(256); // Use 256-bit AES key
                    javax.crypto.SecretKey aesKey = keyGen.generateKey();

                    // Create AES instance for file encryption
                    EncryptionAlgorithm aesInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(
                            "AES", "CBC", "PKCS5Padding", 256);

                    // Encrypt the file with AES
                    byte[] encryptedFile = aesInstance.encrypt(inputData, aesKey);

                    // Encrypt the AES key with RSA
                    EncryptionAlgorithm rsaInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(
                            algorithm, mode, padding, actualKeySize);
                    byte[] encryptedKey = rsaInstance.encrypt(aesKey.getEncoded(), loadedKey);

                    // Combine encrypted key length (4 bytes), encrypted key, and encrypted file
                    byte[] keyLength = new byte[4];
                    keyLength[0] = (byte) (encryptedKey.length >>> 24);
                    keyLength[1] = (byte) (encryptedKey.length >>> 16);
                    keyLength[2] = (byte) (encryptedKey.length >>> 8);
                    keyLength[3] = (byte) encryptedKey.length;

                    outputData = new byte[4 + encryptedKey.length + encryptedFile.length];
                    System.arraycopy(keyLength, 0, outputData, 0, 4);
                    System.arraycopy(encryptedKey, 0, outputData, 4, encryptedKey.length);
                    System.arraycopy(encryptedFile, 0, outputData, 4 + encryptedKey.length, encryptedFile.length);

                } else {
                    // Extract encrypted key length
                    int keyLength = ((inputData[0] & 0xFF) << 24) |
                            ((inputData[1] & 0xFF) << 16) |
                            ((inputData[2] & 0xFF) << 8) |
                            (inputData[3] & 0xFF);

                    // Extract and decrypt the AES key
                    byte[] encryptedKey = new byte[keyLength];
                    System.arraycopy(inputData, 4, encryptedKey, 0, keyLength);

                    EncryptionAlgorithm rsaInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(
                            algorithm, mode, padding, actualKeySize);
                    byte[] decryptedKeyBytes = rsaInstance.decrypt(encryptedKey, loadedKey);
                    javax.crypto.spec.SecretKeySpec aesKey = new javax.crypto.spec.SecretKeySpec(
                            decryptedKeyBytes, "AES");

                    // Extract and decrypt the file
                    byte[] encryptedFile = new byte[inputData.length - 4 - keyLength];
                    System.arraycopy(inputData, 4 + keyLength, encryptedFile, 0, encryptedFile.length);

                    EncryptionAlgorithm aesInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(
                            "AES", "CBC", "PKCS5Padding", 256);
                    outputData = aesInstance.decrypt(encryptedFile, aesKey);
                }
            } else if (algorithm.equals("Caesar") || algorithm.equals("Vigenere")) {
                // Handle traditional ciphers
                EncryptionAlgorithm algoInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(
                        algorithm, mode, padding, actualKeySize);

                if (algorithm.equals("Caesar")) {
                    String shiftStr = JOptionPane.showInputDialog(this,
                            "<html>Nhập độ dịch chuyển cho Caesar:<br/>" +
                                    "- Phải là số nguyên<br/>" +
                                    "- Giá trị dương: dịch phải<br/>" +
                                    "- Giá trị âm: dịch trái<br/>" +
                                    "Ví dụ: 3 sẽ dịch A->D, B->E, ...</html>");
                    if (shiftStr == null || shiftStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Độ dịch không được để trống.",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        int shift = Integer.parseInt(shiftStr.trim());
                        ((CaesarCipher) algoInstance).setShift(shift);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Độ dịch không hợp lệ. Vui lòng nhập số nguyên.",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else { // Vigenere
                    String keyword = JOptionPane.showInputDialog(this,
                            "<html>Nhập từ khóa cho Vigenere:<br/>" +
                                    "- Chỉ chấp nhận chữ cái (A-Z, a-z)<br/>" +
                                    "- Không chấp nhận số, ký tự đặc biệt<br/>" +
                                    "- Không phân biệt hoa thường<br/>" +
                                    "Ví dụ: SECRET, Key, ...</html>");
                    if (keyword == null || keyword.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Từ khóa không được để trống.",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Validate keyword contains only letters
                    String cleanKeyword = keyword.trim().toUpperCase();
                    if (!cleanKeyword.matches("[A-Z]+")) {
                        JOptionPane.showMessageDialog(this,
                                "Từ khóa chỉ được chứa chữ cái (A-Z, a-z).",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        ((VigenereCipher) algoInstance).setKeyword(cleanKeyword);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Từ khóa không hợp lệ: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if (encrypt) {
                    outputData = algoInstance.encrypt(inputData, null);
                } else {
                    outputData = algoInstance.decrypt(inputData, null);
                }
            } else {
                // Normal encryption/decryption for other algorithms
                EncryptionAlgorithm algoInstance = EncryptionAlgorithmFactory.createAlgorithmForOperation(
                        algorithm, mode, padding, actualKeySize);

                if (encrypt) {
                    outputData = algoInstance.encrypt(inputData, loadedKey);
                } else {
                    outputData = algoInstance.decrypt(inputData, loadedKey);
                }
            }

            // Write output file
            FileUtils.writeFileBytes(outputFilePath, outputData);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            JOptionPane.showMessageDialog(this,
                    String.format("%s thành công!\nInput: %s (%d bytes)\nOutput: %s (%d bytes)\nThời gian: %d ms",
                            operation, selectedInputFile.getName(), inputData.length,
                            new File(outputFilePath).getName(), outputData.length, duration),
                    "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi " + operation + ": " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // --- DropTargetListener Implementation ---

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        // Change border or background on drag enter
        dragDropLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // Restore border on drag exit
        if (selectedInputFile == null) {
            dragDropLabel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 5, 5));
        } else {
            dragDropLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        boolean success = false;
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            List<File> droppedFiles = (List<File>) dtde.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);
            if (droppedFiles != null && !droppedFiles.isEmpty()) {
                // Handle only the first file
                File droppedFile = droppedFiles.get(0);
                if (!droppedFile.isDirectory()) { // Ensure it's a file
                    handleSelectedFile(droppedFile);
                    success = true;
                } else {
                    JOptionPane.showMessageDialog(this, "Chỉ chấp nhận file, không chấp nhận thư mục.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi kéo thả file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        dtde.dropComplete(success);
        // Restore border after drop
        dragExit(null);
    }

    // Main method for testing this panel independently
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("FileEncryptionPanel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new FileEncryptionPanel());
        frame.pack(); // Adjust frame size to panel content
        frame.setSize(600, 600); // Or set a specific size
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}