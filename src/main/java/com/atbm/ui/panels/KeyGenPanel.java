package com.atbm.ui.panels;

import com.atbm.ui.StyleConstants;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import com.atbm.core.encryption.EncryptionAlgorithm;
import com.atbm.core.encryption.EncryptionAlgorithmFactory;
import com.atbm.core.encryption.symmetric.SymmetricEncryption;
import com.atbm.core.encryption.asymmetric.AsymmetricEncryption;
import com.atbm.core.key.KeyManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import com.atbm.utils.KeyUtils;
import com.atbm.core.encryption.symmetric.AESEncryption;
import com.atbm.core.encryption.symmetric.DESEncryption;
import com.atbm.core.encryption.symmetric.DESedeEncryption;
import com.atbm.core.encryption.symmetric.BlowfishEncryption;
import com.atbm.core.encryption.symmetric.ChaCha20Poly1305Encryption;
import com.atbm.core.encryption.asymmetric.RSAEncryption;

// Class này là panel cho việc tạo khóa
public class KeyGenPanel extends JPanel {

    private JTextField generatedKeyField;
    private JComboBox<String> algorithmComboBox;
    private JComboBox<Integer> keySizeComboBox;
    private JButton generateButton;
    private JTextField outputDirField;
    private JTextField baseFileNameField;
    private JButton exportButton;
    private JFileChooser directoryChooser;
    private KeyListPanel keyListPanel;

    // Placeholder cho khóa đã tạo
    private Object generatedKeyObject = null;

    public KeyGenPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = StyleConstants.DEFAULT_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Key generate:");
        titleLabel.setFont(StyleConstants.TITLE_FONT);
        add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        generatedKeyField = new JTextField(40);
        generatedKeyField.setFont(StyleConstants.TEXT_FONT);
        generatedKeyField.setEditable(false);
        generatedKeyField.setPreferredSize(StyleConstants.TEXT_FIELD_SIZE);
        add(generatedKeyField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(createAlgorithmSelectionPanel(), gbc);

        gbc.gridy++;
        generateButton = new JButton("Tạo khóa!");
        generateButton.setFont(StyleConstants.BUTTON_FONT);
        generateButton.setPreferredSize(StyleConstants.BUTTON_SIZE);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(generateButton, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        JPanel exportPanel = createExportPanel();
        add(exportPanel, gbc);

        // Add Action Listeners
        setupActionListeners();
        updateKeySizeOptions();
    }

    public void setKeyListPanel(KeyListPanel panel) {
        this.keyListPanel = panel;
    }

    private JPanel createAlgorithmSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Lựa chọn thuật toán"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = StyleConstants.DEFAULT_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel algoLabel = new JLabel("Thuật toán!");
        algoLabel.setFont(StyleConstants.LABEL_FONT);
        panel.add(algoLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        initializeAlgorithmComboBox();
        algorithmComboBox.setFont(StyleConstants.TEXT_FONT);
        algorithmComboBox.setPreferredSize(StyleConstants.COMBO_BOX_SIZE);
        panel.add(algorithmComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel sizeLabel = new JLabel("Kích thước khóa!");
        sizeLabel.setFont(StyleConstants.LABEL_FONT);
        panel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        keySizeComboBox = new JComboBox<>();
        keySizeComboBox.setFont(StyleConstants.TEXT_FONT);
        keySizeComboBox.setPreferredSize(StyleConstants.COMBO_BOX_SIZE);
        panel.add(keySizeComboBox, gbc);

        return panel;
    }

    private void initializeAlgorithmComboBox() {
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
        algorithmComboBox.addActionListener(e -> updateKeySizeComboBox());
    }

    private void updateKeySizeComboBox() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>();

        switch (selectedAlgorithm) {
            case "AES":
                for (int size : AESEncryption.SUPPORTED_KEY_SIZES) {
                    model.addElement(size);
                }
                break;
            case "DES":
                model.addElement(DESEncryption.KEY_SIZE);
                break;
            case "DESede":
                for (int size : DESedeEncryption.SUPPORTED_KEY_SIZES) {
                    model.addElement(size);
                }
                break;
            case "Blowfish":
                for (int size : BlowfishEncryption.SUPPORTED_KEY_SIZES) {
                    model.addElement(size);
                }
                break;
            case "ChaCha20-Poly1305":
                model.addElement(ChaCha20Poly1305Encryption.KEY_SIZE);
                keySizeComboBox.setEnabled(false); // Chỉ hỗ trợ 256-bit
                break;
            case "Twofish":
                model.addElement(128);
                model.addElement(192);
                model.addElement(256);
                keySizeComboBox.setEnabled(true);
                break;
            case "Camellia":
                model.addElement(128);
                model.addElement(192);
                model.addElement(256);
                keySizeComboBox.setEnabled(true);
                break;
            case "CAST5":
                model.addElement(40);
                model.addElement(64);
                model.addElement(80);
                model.addElement(96);
                model.addElement(112);
                model.addElement(128);
                keySizeComboBox.setEnabled(true);
                break;
            case "RC5":
                model.addElement(64);
                model.addElement(128);
                model.addElement(192);
                model.addElement(256);
                keySizeComboBox.setEnabled(true);
                break;
            case "RSA":
                for (int size : RSAEncryption.SUPPORTED_KEY_SIZES) {
                    model.addElement(size);
                }
                keySizeComboBox.setEnabled(true);
                break;
            // Ẩn key size cho thuật toán truyền thống
            case "Caesar":
            case "Vigenere":
                keySizeComboBox.setEnabled(false);
                break;
            default:
                break;
        }

        keySizeComboBox.setModel(model);
        keySizeComboBox.setEnabled(!selectedAlgorithm.equals("Caesar") &&
                !selectedAlgorithm.equals("Vigenere") &&
                !selectedAlgorithm.equals("Monoalphabetic") &&
                !selectedAlgorithm.equals("Affine") &&
                !selectedAlgorithm.equals("Hill"));
    }

    // Tạo panel cho việc xuất kết quả
    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Xuất kết quả"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = StyleConstants.DEFAULT_INSETS;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JButton chooseDirButton = new JButton("Chọn thư mục");
        chooseDirButton.setFont(StyleConstants.BUTTON_FONT);
        chooseDirButton.setPreferredSize(StyleConstants.BUTTON_SIZE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(chooseDirButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        outputDirField = new JTextField("./keys");
        outputDirField.setFont(StyleConstants.TEXT_FONT);
        outputDirField.setPreferredSize(StyleConstants.TEXT_FIELD_SIZE);
        panel.add(outputDirField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.8;
        baseFileNameField = new JTextField();
        baseFileNameField.setFont(StyleConstants.TEXT_FONT);
        baseFileNameField.setPreferredSize(StyleConstants.TEXT_FIELD_SIZE);
        panel.add(baseFileNameField, gbc);
        updateBaseFileName();

        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel extensionLabel = new JLabel(".keys");
        extensionLabel.setFont(StyleConstants.LABEL_FONT);
        panel.add(extensionLabel, gbc);

        gbc.gridx = 3;
        exportButton = new JButton("Xuất khóa");
        exportButton.setFont(StyleConstants.BUTTON_FONT);
        exportButton.setPreferredSize(StyleConstants.BUTTON_SIZE);
        panel.add(exportButton, gbc);

        directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooseDirButton.addActionListener(e -> {
            int returnValue = directoryChooser.showOpenDialog(KeyGenPanel.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDir = directoryChooser.getSelectedFile();
                outputDirField.setText(selectedDir.getAbsolutePath());
            }
        });

        return panel;
    }

    // Thêm Action Listeners
    private void setupActionListeners() {
        algorithmComboBox.addActionListener(e -> {
            updateKeySizeOptions();
            updateBaseFileName();
            generatedKeyObject = null;
            generatedKeyField.setText("");
        });

        keySizeComboBox.addActionListener(e -> {
            updateBaseFileName();
            generatedKeyObject = null;
            generatedKeyField.setText("");
        });

        generateButton.addActionListener(e -> generateKeyAction());
        exportButton.addActionListener(e -> exportKeyAction());
    }

    // Cập nhật kích thước khóa
    private void updateKeySizeOptions() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        keySizeComboBox.removeAllItems();

        if (selectedAlgorithm != null) {
            switch (selectedAlgorithm) {
                case "AES":
                    keySizeComboBox.addItem(128);
                    keySizeComboBox.addItem(192);
                    keySizeComboBox.addItem(256);
                    keySizeComboBox.setEnabled(true);
                    break;
                case "ChaCha20-Poly1305":
                    keySizeComboBox.addItem(256);
                    keySizeComboBox.setEnabled(false);
                    break;
                case "DESede":
                    keySizeComboBox.addItem(112);
                    keySizeComboBox.addItem(168);
                    keySizeComboBox.setSelectedItem(168);
                    keySizeComboBox.setEnabled(true);
                    break;
                case "Camellia":
                    keySizeComboBox.addItem(128);
                    keySizeComboBox.addItem(192);
                    keySizeComboBox.addItem(256);
                    keySizeComboBox.setEnabled(true);
                    break;
                case "RSA":
                    keySizeComboBox.addItem(1024);
                    keySizeComboBox.addItem(2048);
                    keySizeComboBox.addItem(4096);
                    keySizeComboBox.setSelectedItem(2048);
                    keySizeComboBox.setEnabled(true);
                    break;
                case "Caesar":
                case "Vigenere":
                    keySizeComboBox.setEnabled(false);
                    break;
                default:
                    break;
            }
            updateBaseFileName();
        }
    }

    private void updateBaseFileName() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        Integer keySize = (Integer) keySizeComboBox.getSelectedItem();
        if (algorithm != null && keySize != null) {
            baseFileNameField.setText(algorithm + "_" + keySize);
        } else if (algorithm != null) {
            baseFileNameField.setText(algorithm);
        } else {
            baseFileNameField.setText("");
        }
    }

    // Tạo khóa
    private void generateKeyAction() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        Integer keySize = (Integer) keySizeComboBox.getSelectedItem();

        // Thêm xử lý cho thuật toán truyền thống
        if ("Caesar".equals(algorithm) || "Vigenere".equals(algorithm) ||
                "Monoalphabetic".equals(algorithm) || "Affine".equals(algorithm) || "Hill".equals(algorithm)) {
            String key = KeyUtils.generateTraditionalKey(algorithm);
            generatedKeyField.setText(key);
            generatedKeyObject = key;
            return;
        }

        boolean isTraditional = !isSymmetric(algorithm) && !isAsymmetric(algorithm);
        if (algorithm == null || (!isTraditional && keySize == null)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn thuật toán" + (isTraditional ? "." : " và kích thước khóa."), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            EncryptionAlgorithm algoInstance = EncryptionAlgorithmFactory.createAlgorithm(algorithm);

            if (algoInstance instanceof SymmetricEncryption) {
                SymmetricEncryption symAlgo = (SymmetricEncryption) algoInstance;
                SecretKey key = symAlgo.generateKey();
                generatedKeyObject = key;
                // Hiển thị thông tin khóa
                generatedKeyField.setText(String.format("%s Key [%d bits] generated (in memory)",
                        key.getAlgorithm(), keySize));
                JOptionPane.showMessageDialog(this, "Khóa đối xứng đã được tạo thành công (trong bộ nhớ)!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else if (algoInstance instanceof AsymmetricEncryption) {
                AsymmetricEncryption asymAlgo = (AsymmetricEncryption) algoInstance;
                KeyPair keyPair = asymAlgo.generateKeyPair();
                generatedKeyObject = keyPair;
                // Hiển thị thông tin khóa
                generatedKeyField.setText(String.format("%s KeyPair [%d bits] generated (in memory)",
                        keyPair.getPublic().getAlgorithm(), keySize));
                JOptionPane.showMessageDialog(this, "Cặp khóa bất đối xứng đã được tạo thành công (trong bộ nhớ)!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {

                generatedKeyField.setText(algorithm + " selected. No key generation needed.");
                generatedKeyObject = null;
                JOptionPane.showMessageDialog(this, "Thuật toán cổ điển được chọn. Không cần tạo khóa.", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            // Cập nhật danh sách khóa sau khi tạo khóa thành công
            if (keyListPanel != null) {
                keyListPanel.refreshKeyList();
            }

        } catch (Exception ex) {
            generatedKeyField.setText("Lỗi tạo khóa");
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo khóa: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            generatedKeyObject = null;
        }
    }

    // Xuất khóa
    private void exportKeyAction() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();

        if ("Caesar".equals(algorithm) || "Vigenere".equals(algorithm) ||
                "Monoalphabetic".equals(algorithm) || "Affine".equals(algorithm) || "Hill".equals(algorithm)) {
            String key = (String) generatedKeyObject;

            String defaultFileName = algorithm + ".key";
            JFileChooser fileChooser = new JFileChooser();

            File keyDir = new File(System.getProperty("user.dir"), "keys");
            if (!keyDir.exists())
                keyDir.mkdirs();
            fileChooser.setCurrentDirectory(keyDir);

            fileChooser.setSelectedFile(new File(keyDir, defaultFileName));
            fileChooser.setDialogTitle("Lưu khóa truyền thống");

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file.exists()) {
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "File đã tồn tại. Bạn có muốn ghi đè không?",
                            "Cảnh báo",
                            JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                try {
                    Files.write(file.toPath(), key.getBytes(StandardCharsets.UTF_8));
                    JOptionPane.showMessageDialog(this, "Đã lưu key truyền thống!");
                    if (keyListPanel != null) {
                        keyListPanel.refreshKeyList();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu key: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }

        if (generatedKeyObject == null) {
            if (isSymmetric(algorithm) || isAsymmetric(algorithm)) {
                JOptionPane.showMessageDialog(this, "Vui lòng tạo khóa đối xứng/bất đối xứng trước khi xuất.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                JOptionPane.showMessageDialog(this, "Xuất file không áp dụng cho thuật toán này.", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        String dirPath = outputDirField.getText().trim();
        String baseName = baseFileNameField.getText().trim();

        if (dirPath.isEmpty() || baseName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thư mục và nhập tên file cơ sở.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        File outputDir = new File(dirPath);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                JOptionPane.showMessageDialog(this, "Không thể tạo thư mục: " + dirPath, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (!outputDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Đường dẫn xuất không phải là thư mục hợp lệ.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (generatedKeyObject instanceof SecretKey) {
                String filePath = dirPath + File.separator + baseName + ".key";
                File file = new File(filePath);
                if (file.exists()) {
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "File " + file.getName() + " đã tồn tại. Bạn có muốn ghi đè không?",
                            "Cảnh báo",
                            JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                KeyManager.saveKey((SecretKey) generatedKeyObject, filePath);
                JOptionPane.showMessageDialog(this, "Xuất khóa đối xứng thành công tới:\n" + filePath, "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                if (keyListPanel != null) {
                    keyListPanel.refreshKeyList();
                }

            } else if (generatedKeyObject instanceof KeyPair) {
                String publicKeyPath = dirPath + File.separator + baseName + ".pub";
                String privateKeyPath = dirPath + File.separator + baseName + ".pri";
                File pubFile = new File(publicKeyPath);
                File priFile = new File(privateKeyPath);
                if (pubFile.exists() || priFile.exists()) {
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "Một hoặc cả hai file khóa (" + pubFile.getName() + ", " + priFile.getName()
                                    + ") đã tồn tại. Bạn có muốn ghi đè không?",
                            "Cảnh báo",
                            JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                KeyManager.saveKeyPair((KeyPair) generatedKeyObject, publicKeyPath, privateKeyPath);
                JOptionPane.showMessageDialog(this,
                        "Xuất cặp khóa bất đối xứng thành công tới: \n" + publicKeyPath + "\n" + privateKeyPath,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                if (keyListPanel != null) {
                    keyListPanel.refreshKeyList();
                }

            } else {
                JOptionPane.showMessageDialog(this, "Loại khóa không xác định để xuất.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi I/O khi xuất khóa: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi không xác định khi xuất khóa: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Helper methods để xác định loại thuật toán
    private boolean isSymmetric(String algorithm) {
        if (algorithm == null)
            return false;
        return algorithm.equals("AES") || algorithm.equals("DESede") || algorithm.equals("Camellia");
    }

    private boolean isAsymmetric(String algorithm) {
        if (algorithm == null)
            return false;
        return algorithm.equals("RSA");
    }

    // Main method để test độc lâp
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("KeyGenPanel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new KeyGenPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}