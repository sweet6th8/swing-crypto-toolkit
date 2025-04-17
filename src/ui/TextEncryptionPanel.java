package ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

// Placeholder imports
// import model.encryption.EncryptionAlgorithm;
// import model.key.KeyManager;
// import utils.StringUtils;

// Import project classes
import model.encryption.EncryptionAlgorithm;
import model.encryption.EncryptionAlgorithmFactory;
import model.key.KeyManager;
// Import traditional cipher classes if needed for specific handling
import model.encryption.CaesarCipher;
import model.encryption.VigenereCipher;

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
                "AES", "DESede", "ChaCha20Poly1305", "RSA", "Caesar", "Vigenere"
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
        algorithmComboBox.addActionListener(e -> updateModesAndPaddings());
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
            loadedKey = null; // Reset previous key

            try {
                boolean forEncryption = true; // Default assumption
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
                } else if (upperAlgo.equals("AES")) { // Add other symmetric algos here
                    if (!lowerPath.endsWith(".key")) {
                        throw new IllegalArgumentException("Tệp khóa " + upperAlgo + " phải có đuôi .key.");
                    }
                } else if (upperAlgo.equals("CAESAR") || upperAlgo.equals("VIGENERE")) {
                    JOptionPane.showMessageDialog(this, "Load khóa từ file không áp dụng cho " + selectedAlgorithm,
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                loadedKey = KeyManager.loadKeyForOperation(keyFilePath, selectedAlgorithm, forEncryption);

                if (loadedKey != null) {
                    JOptionPane.showMessageDialog(this, "Load khóa thành công!\nThuật toán: " + loadedKey.getAlgorithm()
                            + "\nĐịnh dạng: " + loadedKey.getFormat(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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

    private void updateModesAndPaddings() {
        // Reusing the same logic as FileEncryptionPanel
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        modeComboBox.removeAllItems();
        paddingComboBox.removeAllItems();

        if (selectedAlgorithm != null) {
            boolean isSymmetric = selectedAlgorithm.equals("AES") || selectedAlgorithm.equals("DESede")
                    || selectedAlgorithm.equals("ChaCha20Poly1305");
            boolean isAsymmetric = selectedAlgorithm.equals("RSA");

            if (isSymmetric || selectedAlgorithm.equals("ChaCha20Poly1305")) {
                modeComboBox.addItem("ECB");
                modeComboBox.addItem("CBC");
                modeComboBox.addItem("CFB");
                modeComboBox.addItem("OFB");
                paddingComboBox.addItem("PKCS5Padding");
                paddingComboBox.addItem("NoPadding");
                modeComboBox.setEnabled(true);
                paddingComboBox.setEnabled(true);
                if (selectedAlgorithm.equals("ChaCha20Poly1305")) {
                    modeComboBox.setSelectedItem("None");
                    paddingComboBox.setSelectedItem("NoPadding");
                    modeComboBox.setEnabled(false);
                    paddingComboBox.setEnabled(false);
                }
            } else if (isAsymmetric) {
                modeComboBox.addItem("ECB");
                modeComboBox.setEnabled(false);
                paddingComboBox.addItem("PKCS1Padding");
                paddingComboBox.addItem("OAEPWithSHA-1AndMGF1Padding");
                paddingComboBox.addItem("OAEPWithSHA-256AndMGF1Padding");
                paddingComboBox.addItem("NoPadding");
                paddingComboBox.setEnabled(true);
            } else { // Assume Traditional
                modeComboBox.addItem("ECB");
                paddingComboBox.addItem("NoPadding");
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
        String inputText = inputTextArea.getText();
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        String mode = (String) modeComboBox.getSelectedItem();
        String padding = (String) paddingComboBox.getSelectedItem();
        outputTextArea.setText(""); // Clear previous output

        // --- Input Validation ---
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập văn bản đầu vào.", "Lỗi " + operation,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (loadedKey == null) {
            if (algorithm != null && (algorithm.equalsIgnoreCase("Caesar") || algorithm.equalsIgnoreCase("Vigenere"))) {
                // Allow proceeding for traditional
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng load file key.", "Lỗi " + operation,
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (algorithm == null || (modeComboBox.isEnabled() && mode == null)
                || (paddingComboBox.isEnabled() && padding == null)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn đầy đủ thuật toán" + (modeComboBox.isEnabled() ? ", mode" : "")
                            + (paddingComboBox.isEnabled() ? " và padding." : "."),
                    "Lỗi " + operation, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Key Type Validation (Similar to FileEncryptionPanel)
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
            // --- Determine actual key size from loaded key ---
            int actualKeySize = 0;
            if (loadedKey instanceof java.security.interfaces.RSAKey) {
                // Get nominal key size for RSA keys
                actualKeySize = ((java.security.interfaces.RSAKey) loadedKey).getModulus().bitLength();
            } else if (loadedKey instanceof javax.crypto.SecretKey) {
                // For symmetric keys, encoding length * 8 is correct
                byte[] encodedKey = loadedKey.getEncoded();
                if (encodedKey != null) {
                    actualKeySize = encodedKey.length * 8;
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

            if (algorithm.equals("Caesar") || algorithm.equals("Vigenere")) {
                // Handle traditional ciphers
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
                    inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
                    outputBytes = algoInstance.encrypt(inputBytes, null);
                    outputText = new String(outputBytes, StandardCharsets.UTF_8);
                } else {
                    inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
                    outputBytes = algoInstance.decrypt(inputBytes, null);
                    outputText = new String(outputBytes, StandardCharsets.UTF_8);
                }
            } else {
                // Normal modern encryption
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
            }

            outputTextArea.setText(outputText);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            JOptionPane.showMessageDialog(this,
                    String.format("%s văn bản thành công! (Thời gian: %d ms)", operation, duration),
                    "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            outputTextArea.setText("Lỗi khi " + operation + ": " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi " + operation + ": " + ex.getMessage(), "Lỗi",
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
}