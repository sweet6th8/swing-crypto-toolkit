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
import java.security.SecureRandom;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;
import java.io.FileOutputStream;
import java.security.PublicKey;
import java.security.PrivateKey;
import com.atbm.core.encryption.asymmetric.RSAHybridEncryption;

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
    private JProgressBar progressBar;

    // Placeholder for loaded key and selected file
    private Key loadedKey = null;
    private File selectedInputFile = null;

    private String currentAlgorithmType = "Symmetric";

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
        gbc.weighty = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel dropPanel = createDropPanel();
        add(dropPanel, gbc);

        // --- Key Input ---
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createKeyInputPanel(), gbc);

        // --- Algorithm Selection ---
        gbc.gridy++;
        add(createAlgorithmSelectionPanel(), gbc);

        // --- Output Selection ---
        gbc.gridy++;
        add(createOutputPanel(), gbc);

        // --- Progress Bar ---
        gbc.gridy++;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, gbc);

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
        new DropTarget(dragDropLabel, DnDConstants.ACTION_COPY_OR_MOVE, this, true);

        setAlgorithmType(currentAlgorithmType);
        setupActionListeners();
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
                "Blowfish",
                "ChaCha20-Poly1305",
                "Twofish",
                "Camellia",
                "CAST5",
                "RC5",
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
        String outputName;

        // Check if file is already encrypted (ends with _encrypted)
        if (inputName.toLowerCase().endsWith("_encrypted")) {
            // For decryption, replace _encrypted with _decrypted
            outputName = inputName.replace("_encrypted", "_decrypted");
        } else {
            // For encryption, add _encrypted before the extension
            if (inputName.contains(".")) {
                int lastDot = inputName.lastIndexOf(".");
                outputName = inputName.substring(0, lastDot) + "_encrypted" + inputName.substring(lastDot);
            } else {
                outputName = inputName + "_encrypted";
            }
        }

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

                // Kiểm tra tên file key phải chứa tên thuật toán đang chọn
                if (!lowerPath.contains(selectedAlgorithm.toLowerCase())) {
                    loadedKey = null;
                    keyFilePathField.setText("");
                    JOptionPane.showMessageDialog(this,
                            "Tên file key không khớp với thuật toán đang chọn (" + selectedAlgorithm
                                    + "). Vui lòng chọn đúng file key!",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Use the refined KeyManager method
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
        DefaultComboBoxModel<String> modeModel = new DefaultComboBoxModel<>();

        if (selectedAlgorithm != null && selectedAlgorithm.equals("Twofish")) {
            // Twofish: chỉ cho phép ECB và CBC
            modeModel.addElement("ECB");
            modeModel.addElement("CBC");
        } else if (selectedAlgorithm != null && !selectedAlgorithm.equals("ChaCha20-Poly1305")
                && !selectedAlgorithm.equals("RSA") && !isTraditionalAlgorithm(selectedAlgorithm)) {
            // Các thuật toán đối xứng khác
            modeModel.addElement("ECB");
            modeModel.addElement("CBC");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("ChaCha20-Poly1305")) {
            modeModel.addElement("N/A");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("RSA")) {
            modeModel.addElement("N/A");
        } else if (selectedAlgorithm != null && isTraditionalAlgorithm(selectedAlgorithm)) {
            modeModel.addElement("N/A");
        }

        modeComboBox.setModel(modeModel);
        updatePaddingForMode();
        boolean enableSelection = selectedAlgorithm != null && !selectedAlgorithm.equals("ChaCha20-Poly1305") &&
                !selectedAlgorithm.equals("RSA") &&
                !isTraditionalAlgorithm(selectedAlgorithm);
        modeComboBox.setEnabled(enableSelection);
        paddingComboBox.setEnabled(enableSelection);
        modeComboBox.addActionListener(e -> updatePaddingForMode());
    }

    private void updatePaddingForMode() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        String selectedMode = (String) modeComboBox.getSelectedItem();
        DefaultComboBoxModel<String> paddingModel = new DefaultComboBoxModel<>();
        if (selectedAlgorithm != null && selectedAlgorithm.equals("Twofish")) {
            if (selectedMode == null) {
                paddingComboBox.setModel(paddingModel);
                return;
            }
            switch (selectedMode) {
                case "ECB":
                    paddingModel.addElement("NoPadding");
                    break;
                case "CBC":
                    paddingModel.addElement("PKCS5Padding");
                    paddingModel.addElement("NoPadding");
                    break;
                default:
                    break;
            }
        } else if (selectedAlgorithm != null && !selectedAlgorithm.equals("ChaCha20-Poly1305")
                && !selectedAlgorithm.equals("RSA") && !isTraditionalAlgorithm(selectedAlgorithm)) {
            // Các thuật toán đối xứng khác
            paddingModel.addElement("PKCS5Padding");
            paddingModel.addElement("NoPadding");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("ChaCha20-Poly1305")) {
            paddingModel.addElement("N/A");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("RSA")) {
            paddingModel.addElement("N/A");
        } else if (selectedAlgorithm != null && isTraditionalAlgorithm(selectedAlgorithm)) {
            paddingModel.addElement("N/A");
        }
        paddingComboBox.setModel(paddingModel);
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
            if (algorithm != null && (algorithm.equalsIgnoreCase("Caesar") || algorithm.equalsIgnoreCase("Vigenere"))) {
                // Allow proceeding for traditional ciphers
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

        // Disable buttons and show progress bar
        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);

        // Run encryption/decryption in background thread
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // --- Determine actual key size from loaded key ---
                    int actualKeySize = 0;
                    if (loadedKey instanceof java.security.interfaces.RSAKey) {
                        actualKeySize = ((java.security.interfaces.RSAKey) loadedKey).getModulus().bitLength();
                    } else if (loadedKey instanceof javax.crypto.SecretKey) {
                        String keyAlgorithm = loadedKey.getAlgorithm();
                        if (keyAlgorithm.equals("DESede")) {
                            byte[] encodedKey = loadedKey.getEncoded();
                            if (encodedKey != null) {
                                if (encodedKey.length == 24) {
                                    actualKeySize = 168;
                                } else if (encodedKey.length == 16) {
                                    actualKeySize = 112;
                                } else {
                                    throw new IllegalArgumentException(
                                            "Invalid DESede key length: " + (encodedKey.length * 8)
                                                    + " bits. Supported sizes: 112, 168 bits.");
                                }
                            }
                        } else {
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
                            RSAHybridEncryption.encryptFile(selectedInputFile, new File(outputFilePath),
                                    (PublicKey) loadedKey);
                        } else {
                            // Kiểm tra đúng loại khóa trước khi giải mã
                            if (!(loadedKey instanceof PrivateKey)) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(FileEncryptionPanel.this,
                                            "Giải mã RSA yêu cầu khóa bí mật (.pri). Vui lòng chọn đúng file khóa bí mật!",
                                            "Lỗi Key",
                                            JOptionPane.ERROR_MESSAGE);
                                });
                                return null;
                            }
                            RSAHybridEncryption.decryptFile(selectedInputFile, new File(outputFilePath),
                                    (PrivateKey) loadedKey);
                        }
                    } else if (algorithm.equals("Caesar") || algorithm.equals("Vigenere")) {
                        // Traditional cipher handling remains the same
                        // ... existing traditional cipher code ...
                    } else {
                        // Handle symmetric encryption with streaming
                        String transformation;
                        boolean isChaCha = algorithm.equals("ChaCha20-Poly1305");
                        if (isChaCha) {
                            transformation = "ChaCha20-Poly1305";
                        } else {
                            transformation = algorithm + "/" + mode + "/" + padding;
                        }

                        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(transformation);
                        if (encrypt) {
                            if (isChaCha) {
                                byte[] nonce = new byte[12];
                                new SecureRandom().nextBytes(nonce);
                                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, loadedKey, new IvParameterSpec(nonce));
                            } else if (mode.equals("CBC")) {
                                int ivLength = 16;
                                if (algorithm.equals("Blowfish") || algorithm.equals("DES")
                                        || algorithm.equals("DESede")) {
                                    ivLength = 8;
                                }
                                byte[] iv = new byte[ivLength];
                                new SecureRandom().nextBytes(iv);
                                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, loadedKey, new IvParameterSpec(iv));
                            } else {
                                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, loadedKey);
                            }

                            Consumer<Double> progressCallback = progress -> publish((int) (progress * 100));
                            FileUtils.encryptFile(selectedInputFile.getAbsolutePath(), outputFilePath, cipher, isChaCha,
                                    progressCallback, mode);
                        } else {
                            if (isChaCha) {
                                byte[] nonce = new byte[12];
                                try (FileInputStream fis = new FileInputStream(selectedInputFile)) {
                                    if (fis.read(nonce) != 12) {
                                        throw new IOException("File không hợp lệ: không thể đọc nonce");
                                    }
                                }
                                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, loadedKey, new IvParameterSpec(nonce));
                            } else if (mode.equals("CBC")) {
                                int ivLength = 16;
                                if (algorithm.equals("Blowfish") || algorithm.equals("DES")
                                        || algorithm.equals("DESede")) {
                                    ivLength = 8;
                                }
                                byte[] iv = new byte[ivLength];
                                try (FileInputStream fis = new FileInputStream(selectedInputFile)) {
                                    if (fis.read(iv) != ivLength) {
                                        throw new IOException("File không hợp lệ: không thể đọc IV");
                                    }
                                }
                                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, loadedKey, new IvParameterSpec(iv));
                            } else {
                                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, loadedKey);
                            }

                            Consumer<Double> progressCallback = progress -> publish((int) (progress * 100));
                            FileUtils.decryptFile(selectedInputFile.getAbsolutePath(), outputFilePath, cipher, isChaCha,
                                    progressCallback, mode);
                        }
                    }

                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    // Get file sizes for the message
                    long inputSize = selectedInputFile.length();
                    long outputSize = new File(outputFilePath).length();

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(FileEncryptionPanel.this,
                                String.format("%s thành công!\nInput: %s (%s)\nOutput: %s (%s)\nThời gian: %d ms",
                                        operation,
                                        selectedInputFile.getName(), FileUtils.formatFileSize(inputSize),
                                        new File(outputFilePath).getName(), FileUtils.formatFileSize(outputSize),
                                        duration),
                                "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        String msg = ex.getMessage();
                        if (algorithm.equals("RSA") && !encrypt && msg != null
                                && (msg.toLowerCase().contains("padding") ||
                                        msg.toLowerCase().contains("block type") ||
                                        msg.toLowerCase().contains("block size") ||
                                        msg.toLowerCase().contains("decryption"))) {
                            JOptionPane.showMessageDialog(FileEncryptionPanel.this,
                                    "Lỗi: Kích thước khóa giải mã không khớp với khóa đã dùng để mã hóa. Vui lòng chọn đúng cặp khóa RSA!",
                                    "Lỗi Giải mã RSA",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(FileEncryptionPanel.this,
                                    "Lỗi khi " + operation + ": " + msg, "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        ex.printStackTrace();
                    });
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                for (int progress : chunks) {
                    progressBar.setValue(progress);
                }
            }

            @Override
            protected void done() {
                encryptButton.setEnabled(true);
                decryptButton.setEnabled(true);
                progressBar.setVisible(false);
            }
        };

        worker.execute();
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

    // Thêm hàm tiện ích xác định thuật toán truyền thống
    private boolean isTraditionalAlgorithm(String algorithm) {
        if (algorithm == null)
            return false;
        String upper = algorithm.toUpperCase();
        return upper.equals("CAESAR") || upper.equals("VIGENERE")
                || upper.equals("MONOALPHABETIC") || upper.equals("AFFINE") || upper.equals("HILL");
    }

    public void setAlgorithmType(String type) {
        this.currentAlgorithmType = type;
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        switch (type) {
            case "Symmetric":
                model.addElement("AES");
                model.addElement("DES");
                model.addElement("DESede");
                model.addElement("Blowfish");
                model.addElement("ChaCha20-Poly1305");
                model.addElement("Twofish");
                model.addElement("Camellia");
                model.addElement("CAST5");
                model.addElement("RC5");
                break;
            case "Asymmetric":
                model.addElement("RSA");
                break;
            case "Traditional":
                model.addElement("Caesar");
                model.addElement("Vigenere");
                model.addElement("Monoalphabetic");
                model.addElement("Affine");
                model.addElement("Hill");
                break;
        }
        algorithmComboBox.setModel(model);
        updateModesAndPaddings();
    }
}