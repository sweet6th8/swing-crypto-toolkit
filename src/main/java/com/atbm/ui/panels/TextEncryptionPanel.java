package com.atbm.ui.panels;

import com.atbm.core.encryption.EncryptionAlgorithm;
import com.atbm.core.encryption.EncryptionAlgorithmFactory;
import com.atbm.core.key.KeyManager;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

// Class này là panel mã hóa/giải mã văn bản
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

    private Key loadedKey = null;
    private String loadedTraditionalKey = null;

    private String currentAlgorithmType = "Symmetric";

    public TextEncryptionPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;

        fileChooser = new JFileChooser();

        // Nhập văn bản
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4;
        inputTextArea = new JTextArea();
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder("Nhập văn bản"));
        add(inputScrollPane, gbc);

        // Xuất văn bản
        gbc.gridy++;
        outputTextArea = new JTextArea();
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Văn bản đã mã hóa/giải mã"));
        add(outputScrollPane, gbc);

        // Nhập file key
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createKeyInputPanel(), gbc);

        // Chọn thuật toán
        gbc.gridy++;
        add(createAlgorithmSelectionPanel(), gbc);

        // Button mã hóa/giải mã
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
        setAlgorithmType(currentAlgorithmType);
    }

    // Tạo panel nhập file key
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

    // Tạo panel chọn thuật toán
    private JPanel createAlgorithmSelectionPanel() {
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

    // Thiết lập các listener
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

    // Load file key
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

                    String keyText = new String(java.nio.file.Files.readAllBytes(keyFile.toPath()),
                            java.nio.charset.StandardCharsets.UTF_8);
                    if (keyText.startsWith("Khóa")) {
                        keyText = keyText.substring(keyText.indexOf(":") + 1).trim();
                    }
                    loadedTraditionalKey = keyText;
                    JOptionPane.showMessageDialog(this, "Load khóa truyền thống thành công!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
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

    // Cập nhật mode và padding
    private void updateModesAndPaddings() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        DefaultComboBoxModel<String> modeModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<String> paddingModel = new DefaultComboBoxModel<>();

        if (selectedAlgorithm != null) {
            if (selectedAlgorithm.equals("ChaCha20-Poly1305")) {
                // ChaCha20-Poly1305 không cần mode và padding
                modeModel.addElement("N/A");
                paddingModel.addElement("N/A");
            } else if (selectedAlgorithm.equals("RSA")) {
                // RSA không cần mode và padding
                modeModel.addElement("N/A");
                paddingModel.addElement("N/A");
            } else if (isTraditionalAlgorithm(selectedAlgorithm)) {
                // Thuật toán truyền thống không cần mode và padding
                modeModel.addElement("N/A");
                paddingModel.addElement("N/A");
            } else {
                // Các thuật toán đối xứng (bao gồm Twofish)
                modeModel.addElement("ECB");
                modeModel.addElement("CBC");
                paddingModel.addElement("PKCS5Padding");
                paddingModel.addElement("NoPadding");
            }
        }

        modeComboBox.setModel(modeModel);
        paddingComboBox.setModel(paddingModel);

        // Tắt chọn mode và padding cho các thuật toán không sử dụng chúng
        boolean enableSelection = !selectedAlgorithm.equals("ChaCha20-Poly1305") &&
                !selectedAlgorithm.equals("RSA") &&
                !isTraditionalAlgorithm(selectedAlgorithm);
        modeComboBox.setEnabled(enableSelection);
        paddingComboBox.setEnabled(enableSelection);
    }

    // Thực hiện mã hóa/giải mã
    private void performEncryptionDecryption(boolean encrypt) {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        String mode = (String) modeComboBox.getSelectedItem();
        String padding = (String) paddingComboBox.getSelectedItem();
        String inputText = inputTextArea.getText();
        String upperAlgo = algorithm != null ? algorithm.toUpperCase() : "";

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

        if (loadedKey != null) {
            String keyAlgorithm = loadedKey.getAlgorithm();
            String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
            if (!keyAlgorithm.equalsIgnoreCase(selectedAlgorithm)) {
                JOptionPane.showMessageDialog(this,
                        "Key bạn đang dùng không phù hợp với thuật toán đang chọn (" + selectedAlgorithm
                                + "). Vui lòng load đúng file key!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // kiểm tra loại key
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
        } else if (upperAlgo.equals("AES")) {
            if (!(loadedKey instanceof javax.crypto.SecretKey)) {
                JOptionPane.showMessageDialog(this, "Thao tác " + upperAlgo + " yêu cầu khóa Bí mật (.key).", "Lỗi Key",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

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
            if (encrypt) {
                inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
                outputBytes = algoInstance.encrypt(inputBytes, loadedKey);
                outputText = Base64.getEncoder().encodeToString(outputBytes);
            } else {
                try {
                    inputBytes = Base64.getDecoder().decode(inputText);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi giải mã Base64: Dữ liệu đầu vào không hợp lệ.",
                            "Lỗi Giải mã", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                outputBytes = algoInstance.decrypt(inputBytes, loadedKey);
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

    // Hafm tiện ích xác định thuật toán truyền thống
    private boolean isTraditionalAlgorithm(String algorithm) {
        if (algorithm == null)
            return false;
        String upper = algorithm.toUpperCase();
        return upper.equals("CAESAR") || upper.equals("VIGENERE")
                || upper.equals("MONOALPHABETIC") || upper.equals("AFFINE") || upper.equals("HILL");
    }

    // Hàm set thuật toán
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

    // Test độc lập
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

}