package com.atbm.ui.panels;

import com.atbm.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.security.Key;
import com.atbm.core.key.KeyManager;
import java.security.SecureRandom;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;
import java.security.PublicKey;
import java.security.PrivateKey;
import com.atbm.core.encryption.asymmetric.RSAHybridEncryption;

// Class mã hóa/giải mã file
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

        // Drop panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel dropPanel = createDropPanel();
        add(dropPanel, gbc);

        // Key input
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createKeyInputPanel(), gbc);

        // Chọn thuật toán
        gbc.gridy++;
        add(createAlgorithmSelectionPanel(), gbc);

        // Xuất kết quả
        gbc.gridy++;
        add(createOutputPanel(), gbc);

        // Progress bar
        gbc.gridy++;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, gbc);

        // Button mã hóa/giải mã
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

        // Kéo thả file
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
        new DropTarget(dragDropLabel, DnDConstants.ACTION_COPY_OR_MOVE, this, true);

        setAlgorithmType(currentAlgorithmType);
        setupActionListeners();
    }

    // Drop panel
    private JPanel createDropPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Chọn File Đầu Vào"));

        dragDropLabel = new JLabel(
                "<html><div style='text-align: center;'>Kéo và thả file vào đây!<br/> </div></html>",
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

    // Key input
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

    // Chọn thuật toán
    private JPanel createAlgorithmSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
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

    // Xuất kết quả
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

    // Action listeners
    private void setupActionListeners() {
        browseInputButton.addActionListener(e -> browseInputFile());
        loadKeyButton.addActionListener(e -> loadKeyFile());
        browseOutputButton.addActionListener(e -> browseOutputFile());

        algorithmComboBox.addActionListener(e -> updateModesAndPaddings());

        encryptButton.addActionListener(e -> performEncryptionDecryption(true));
        decryptButton.addActionListener(e -> performEncryptionDecryption(false));
    }

    // Chọn file đầu vào
    private void browseInputFile() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            handleSelectedFile(fileChooser.getSelectedFile());
        }
    }

    // Xử lý file
    private void handleSelectedFile(File file) {
        selectedInputFile = file;
        inputFilePathField.setText(file.getAbsolutePath());

        String inputName = file.getName();
        String outputName;

        if (inputName.toLowerCase().endsWith("_encrypted")) {
            outputName = inputName.replace("_encrypted", "_decrypted");
        } else {
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
                }

                if (!lowerPath.contains(selectedAlgorithm.toLowerCase())) {
                    loadedKey = null;
                    keyFilePathField.setText("");
                    JOptionPane.showMessageDialog(this,
                            "Tên file key không khớp với thuật toán đang chọn (" + selectedAlgorithm
                                    + "). Vui lòng chọn đúng file key!",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                loadedKey = KeyManager.loadKeyForOperation(keyFilePath, selectedAlgorithm, forEncryption);

                if (loadedKey != null) {
                    String keyAlgorithm = loadedKey.getAlgorithm();
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
                keyFilePathField.setText("");
                JOptionPane.showMessageDialog(this, "Lỗi khi load khóa: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // Xuất kết quả
    private void browseOutputFile() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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

    // Cập nhật mode và padding
    private void updateModesAndPaddings() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        DefaultComboBoxModel<String> modeModel = new DefaultComboBoxModel<>();

        if (selectedAlgorithm != null && selectedAlgorithm.equals("Twofish")) {
            modeModel.addElement("ECB");
            modeModel.addElement("CBC");
        } else if (selectedAlgorithm != null && !selectedAlgorithm.equals("ChaCha20-Poly1305")
                && !selectedAlgorithm.equals("RSA")) {
            modeModel.addElement("ECB");
            modeModel.addElement("CBC");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("ChaCha20-Poly1305")) {
            modeModel.addElement("N/A");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("RSA")) {
            modeModel.addElement("N/A");
        }

        modeComboBox.setModel(modeModel);
        updatePaddingForMode();
        boolean enableSelection = selectedAlgorithm != null && !selectedAlgorithm.equals("ChaCha20-Poly1305") &&
                !selectedAlgorithm.equals("RSA");
        modeComboBox.setEnabled(enableSelection);
        paddingComboBox.setEnabled(enableSelection);
        modeComboBox.addActionListener(e -> updatePaddingForMode());
    }

    // Cập nhật padding cho mode
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
                && !selectedAlgorithm.equals("RSA")) {
            paddingModel.addElement("PKCS5Padding");
            paddingModel.addElement("NoPadding");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("ChaCha20-Poly1305")) {
            paddingModel.addElement("N/A");
        } else if (selectedAlgorithm != null && selectedAlgorithm.equals("RSA")) {
            paddingModel.addElement("N/A");
        }
        paddingComboBox.setModel(paddingModel);
    }

    // Thực hiện mã hóa/giải mã
    private void performEncryptionDecryption(boolean encrypt) {
        String operation = encrypt ? "Mã hóa" : "Giải mã";
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        String mode = (String) modeComboBox.getSelectedItem();
        String padding = (String) paddingComboBox.getSelectedItem();
        String outputFilePath = outputFilePathField.getText().trim();

        if (selectedInputFile == null || !selectedInputFile.exists()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file đầu vào hợp lệ.", "Lỗi " + operation,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (loadedKey == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng load file key.", "Lỗi " + operation,
                    JOptionPane.ERROR_MESSAGE);
            return;
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

        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);

        // Chạy mã hóa/giải mã trong background thread
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
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

                    if (algorithm.equals("RSA")) {
                        if (encrypt) {
                            RSAHybridEncryption.encryptFile(selectedInputFile, new File(outputFilePath),
                                    (PublicKey) loadedKey);
                        } else {
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
                    } else {
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

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
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
                File droppedFile = droppedFiles.get(0);
                if (!droppedFile.isDirectory()) {
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
        dragExit(null);
    }

    // test
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("FileEncryptionPanel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new FileEncryptionPanel());
        frame.pack();
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
                // Không thêm thuật toán truyền thống
                break;
        }
        algorithmComboBox.setModel(model);
        updateModesAndPaddings();
    }
}