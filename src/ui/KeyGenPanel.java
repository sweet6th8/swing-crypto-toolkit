package ui;

import model.key.KeyManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import javax.crypto.SecretKey;
import java.util.Base64;

// Import model classes
import model.encryption.EncryptionAlgorithm;
import model.encryption.EncryptionAlgorithmFactory;
import model.encryption.SymmetricEncryption;
import model.encryption.AsymmetricEncryption;

public class KeyGenPanel extends JPanel {

    private JTextField generatedKeyField; // Displays generated key or path
    private JComboBox<String> algorithmComboBox;
    private JComboBox<Integer> keySizeComboBox;
    private JButton generateButton;
    private JTextField outputDirField;
    private JTextField baseFileNameField;
    private JButton exportButton;
    private JFileChooser directoryChooser;

    // Placeholder for generated key/keypair
    private Object generatedKeyObject = null;

    public KeyGenPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Generated Key Display ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel("Key generate:"), gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        generatedKeyField = new JTextField(40);
        generatedKeyField.setEditable(false);
        add(generatedKeyField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0; // Reset weight
        // Placeholder button, maybe for copying key?
        // add(new JButton("..."), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(createAlgorithmSelectionPanel(), gbc);

        gbc.gridy++;
        generateButton = new JButton("Tạo khóa!");
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(generateButton, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        add(createExportPanel(), gbc);

        // Add Action Listeners
        setupActionListeners();
        updateKeySizes(); // Initial population of key sizes
    }

    private JPanel createAlgorithmSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lựa chọn thuật toán"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Thuật toán!"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        // TODO: Populate with actual algorithms from model
        algorithmComboBox = new JComboBox<>(new String[] { "AES", "DESede", "ChaCha20Poly1305", "RSA" });
        panel.add(algorithmComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Kích thước khóa!"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        keySizeComboBox = new JComboBox<>();
        panel.add(keySizeComboBox, gbc);

        return panel;
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Xuất kết quả"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Directory Chooser Button
        JButton chooseDirButton = new JButton("Chọn thư mục");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(chooseDirButton, gbc);

        // Directory Path Field
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        outputDirField = new JTextField("./keys"); // Default directory
        panel.add(outputDirField, gbc);

        // Base Filename
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.8; // Give it more space
        baseFileNameField = new JTextField();
        panel.add(baseFileNameField, gbc);
        updateBaseFileName(); // Initial name based on algorithm

        // .keys Label
        gbc.gridx = 2;
        gbc.weightx = 0; // Reset weight
        panel.add(new JLabel(".keys"), gbc);

        // Export Button
        gbc.gridx = 3;
        gbc.weightx = 0;
        exportButton = new JButton("Xuất file");
        panel.add(exportButton, gbc);

        // Setup Directory Chooser
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

    private void setupActionListeners() {
        algorithmComboBox.addActionListener(e -> {
            updateKeySizes();
            updateBaseFileName();
            generatedKeyObject = null; // Reset generated key on algorithm change
            generatedKeyField.setText("");
        });

        keySizeComboBox.addActionListener(e -> {
            updateBaseFileName();
            generatedKeyObject = null; // Reset generated key on key size change
            generatedKeyField.setText("");
        });

        generateButton.addActionListener(e -> generateKeyAction());
        exportButton.addActionListener(e -> exportKeyAction());
    }

    private void updateKeySizes() {
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        keySizeComboBox.removeAllItems();
        if (selectedAlgorithm != null) {
            switch (selectedAlgorithm) {
                case "AES":
                case "ChaCha20Poly1305": // ChaCha requires 256-bit key
                    keySizeComboBox.addItem(128);
                    keySizeComboBox.addItem(192);
                    keySizeComboBox.addItem(256);
                    if (selectedAlgorithm.equals("ChaCha20Poly1305")) {
                        keySizeComboBox.setSelectedItem(256);
                        keySizeComboBox.setEnabled(false); // ChaCha20 key size is fixed
                    } else {
                        keySizeComboBox.setEnabled(true);
                    }
                    break;
                case "DESede": // Triple DES
                    keySizeComboBox.addItem(112); // Effective key size
                    keySizeComboBox.addItem(168); // Actual key size
                    keySizeComboBox.setSelectedItem(168);
                    keySizeComboBox.setEnabled(true);
                    break;
                case "RSA":
                    keySizeComboBox.addItem(1024);
                    keySizeComboBox.addItem(2048);
                    keySizeComboBox.addItem(4096);
                    keySizeComboBox.setSelectedItem(2048);
                    keySizeComboBox.setEnabled(true);
                    break;
                // Add other algorithms and their key sizes here
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

    private void generateKeyAction() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        Integer keySize = (Integer) keySizeComboBox.getSelectedItem();

        // Handle traditional ciphers where key size might not be selected/relevant
        boolean isTraditional = !isSymmetric(algorithm) && !isAsymmetric(algorithm);
        if (algorithm == null || (!isTraditional && keySize == null)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn thuật toán" + (isTraditional ? "." : " và kích thước khóa."), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            generatedKeyObject = null; // Reset before generation
            int actualKeySize = (keySize != null) ? keySize : 0; // Default for traditional

            EncryptionAlgorithm algoInstance = EncryptionAlgorithmFactory.createAlgorithmForKeyGen(algorithm,
                    actualKeySize);

            if (algoInstance instanceof SymmetricEncryption) {
                SymmetricEncryption symAlgo = (SymmetricEncryption) algoInstance;
                SecretKey secretKey = symAlgo.generateKey();
                generatedKeyObject = secretKey;
                // Display key info (e.g., algorithm, size, and part of the key encoded)
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                String truncatedKey = encodedKey.length() > 16 ? encodedKey.substring(0, 16) + "..." : encodedKey;
                generatedKeyField.setText(String.format("%s SecretKey [%d bits]: %s (in memory)",
                        secretKey.getAlgorithm(), secretKey.getEncoded().length * 8, truncatedKey));
                JOptionPane.showMessageDialog(this, "Khóa đối xứng đã được tạo thành công (trong bộ nhớ)!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } else if (algoInstance instanceof AsymmetricEncryption) {
                AsymmetricEncryption asymAlgo = (AsymmetricEncryption) algoInstance;
                KeyPair keyPair = asymAlgo.generateKeyPair();
                generatedKeyObject = keyPair;
                // Display key info
                generatedKeyField.setText(String.format("%s KeyPair [%d bits] generated (in memory)",
                        keyPair.getPublic().getAlgorithm(), actualKeySize));
                JOptionPane.showMessageDialog(this, "Cặp khóa bất đối xứng đã được tạo thành công (trong bộ nhớ)!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Handle Traditional Ciphers - Key generation might not apply
                generatedKeyField.setText(algorithm + " selected. No key generation needed.");
                generatedKeyObject = null; // Or a placeholder if needed for export?
                JOptionPane.showMessageDialog(this, "Thuật toán cổ điển được chọn. Không cần tạo khóa.", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            generatedKeyField.setText("Lỗi tạo khóa");
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo khóa: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Log detailed error
            generatedKeyObject = null; // Ensure object is null on error
        }
    }

    private void exportKeyAction() {
        if (generatedKeyObject == null) {
            // Allow exporting even if no key was *generated*, e.g., for traditional config?
            // Maybe check based on algorithm type instead?
            String algorithm = (String) algorithmComboBox.getSelectedItem();
            if (isSymmetric(algorithm) || isAsymmetric(algorithm)) {
                JOptionPane.showMessageDialog(this, "Vui lòng tạo khóa đối xứng/bất đối xứng trước khi xuất.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                // Handle export for traditional ciphers if needed (e.g., save shift/keyword?)
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
            if (!outputDir.mkdirs()) { // Try to create directory
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
                String filePath = dirPath + File.separator + baseName + ".key"; // Use .key extension
                KeyManager.saveKey((SecretKey) generatedKeyObject, filePath);
                JOptionPane.showMessageDialog(this, "Xuất khóa đối xứng thành công tới:\n" + filePath, "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } else if (generatedKeyObject instanceof KeyPair) {
                String publicKeyPath = dirPath + File.separator + baseName + ".pub"; // Use .pub extension
                String privateKeyPath = dirPath + File.separator + baseName + ".pri"; // Use .pri extension
                KeyManager.saveKeyPair((KeyPair) generatedKeyObject, publicKeyPath, privateKeyPath);
                JOptionPane.showMessageDialog(this,
                        "Xuất cặp khóa bất đối xứng thành công tới: \n" + publicKeyPath + "\n" + privateKeyPath,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } else {
                // Should not happen if check at the beginning is correct
                JOptionPane.showMessageDialog(this, "Loại khóa không xác định để xuất.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi I/O khi xuất khóa: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) { // Catch other potential errors
            JOptionPane.showMessageDialog(this, "Lỗi không xác định khi xuất khóa: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Helper methods to determine algorithm type (replace with better logic if
    // needed)
    private boolean isSymmetric(String algorithm) {
        if (algorithm == null)
            return false;
        // TODO: Update list when adding DESede, ChaCha20
        return algorithm.equals("AES"); // || algorithm.equals("DESede") || algorithm.equals("ChaCha20Poly1305");
    }

    private boolean isAsymmetric(String algorithm) {
        if (algorithm == null)
            return false;
        return algorithm.equals("RSA");
    }

    // Main method for testing this panel independently
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