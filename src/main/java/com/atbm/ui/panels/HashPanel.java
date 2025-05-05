package com.atbm.ui.panels;

import com.atbm.core.hash.HashAlgorithm;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

// Class hash text và file
public class HashPanel extends JPanel implements DropTargetListener {
    private JTabbedPane tabbedPane;
    // Hash text
    private JTextArea inputTextArea;
    private JComboBox<String> algoTextComboBox;
    private JButton hashTextButton;
    private JTextArea resultTextArea;
    private JButton copyTextButton;
    // Hash file
    private JPanel fileDropPanel;
    private JLabel fileDropLabel;
    private File selectedFile;
    private JComboBox<String> algoFileComboBox;
    private JButton hashFileButton;
    private JTextArea resultFileArea;
    private JButton copyFileButton;
    private JProgressBar fileProgressBar;

    public HashPanel() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Hash text", createTextTab());
        tabbedPane.addTab("Hash file", createFileTab());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTextTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JLabel inputLabel = new JLabel("Nhập văn bản");
        inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(inputLabel);
        panel.add(Box.createVerticalStrut(10));

        inputTextArea = new JTextArea(7, 40);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        JScrollPane textScroll = new JScrollPane(inputTextArea);
        panel.add(textScroll);
        panel.add(Box.createVerticalStrut(20));

        JPanel algoPanel = new JPanel();
        algoPanel.setLayout(new BoxLayout(algoPanel, BoxLayout.Y_AXIS));
        JLabel algoLabel = new JLabel("Lựa chọn thuật toán!");
        algoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        algoPanel.add(algoLabel);
        algoPanel.add(Box.createVerticalStrut(5));
        algoTextComboBox = new JComboBox<>(HashAlgorithm.getSupportedAlgorithms());
        algoTextComboBox.setMaximumSize(new Dimension(300, 30));
        algoPanel.add(algoTextComboBox);
        panel.add(algoPanel);
        panel.add(Box.createVerticalStrut(20));

        hashTextButton = new JButton("Hash văn bản");
        hashTextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hashTextButton);
        panel.add(Box.createVerticalStrut(20));

        JLabel resultLabel = new JLabel("Mã hash");
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resultLabel);
        panel.add(Box.createVerticalStrut(10));

        resultTextArea = new JTextArea(3, 40);
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        JScrollPane resultScroll = new JScrollPane(resultTextArea);
        panel.add(resultScroll);
        panel.add(Box.createVerticalStrut(10));

        copyTextButton = new JButton("Copy");
        copyTextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(copyTextButton);

        // Action listeners
        hashTextButton.addActionListener(e -> hashText());
        copyTextButton.addActionListener(e -> copyToClipboard(resultTextArea));

        return panel;
    }

    private JPanel createFileTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        // Drop area
        fileDropPanel = new JPanel(new BorderLayout());
        fileDropPanel.setPreferredSize(new Dimension(400, 180));
        fileDropPanel.setMaximumSize(new Dimension(600, 200));
        fileDropPanel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 3, 6));
        fileDropLabel = new JLabel("Kéo thả file vào đây!", SwingConstants.CENTER);
        fileDropLabel.setFont(new Font(Font.SERIF, Font.ITALIC, 22));
        fileDropPanel.add(fileDropLabel, BorderLayout.CENTER);
        new DropTarget(fileDropPanel, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
        panel.add(fileDropPanel);
        panel.add(Box.createVerticalStrut(20));

        // Progress bar
        fileProgressBar = new JProgressBar(0, 100);
        fileProgressBar.setStringPainted(true);
        fileProgressBar.setVisible(false);
        panel.add(fileProgressBar);
        panel.add(Box.createVerticalStrut(10));

        JPanel algoPanel = new JPanel();
        algoPanel.setLayout(new BoxLayout(algoPanel, BoxLayout.Y_AXIS));
        JLabel algoLabel = new JLabel("Lựa chọn thuật toán!");
        algoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        algoPanel.add(algoLabel);
        algoPanel.add(Box.createVerticalStrut(5));
        algoFileComboBox = new JComboBox<>(HashAlgorithm.getSupportedAlgorithms());
        algoFileComboBox.setMaximumSize(new Dimension(300, 30));
        algoPanel.add(algoFileComboBox);
        panel.add(algoPanel);
        panel.add(Box.createVerticalStrut(20));

        hashFileButton = new JButton("Hash file");
        hashFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hashFileButton);
        panel.add(Box.createVerticalStrut(20));

        JLabel resultLabel = new JLabel("Mã hash");
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resultLabel);
        panel.add(Box.createVerticalStrut(10));

        resultFileArea = new JTextArea(3, 40);
        resultFileArea.setEditable(false);
        resultFileArea.setLineWrap(true);
        resultFileArea.setWrapStyleWord(true);
        resultFileArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        JScrollPane resultScroll = new JScrollPane(resultFileArea);
        panel.add(resultScroll);
        panel.add(Box.createVerticalStrut(10));

        copyFileButton = new JButton("Copy");
        copyFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(copyFileButton);

        // Action listeners
        hashFileButton.addActionListener(e -> hashFileWithProgress());
        copyFileButton.addActionListener(e -> copyToClipboard(resultFileArea));

        return panel;
    }

    // Hash logic
    private void hashText() {
        String text = inputTextArea.getText();
        String algo = (String) algoTextComboBox.getSelectedItem();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập văn bản để hash.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String hash = HashAlgorithm.hashText(text, algo);
            resultTextArea.setText(hash);
        } catch (Exception ex) {
            resultTextArea.setText("");
            JOptionPane.showMessageDialog(this, "Lỗi khi hash văn bản: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // để copy kết quả hash
    private void copyToClipboard(JTextArea area) {
        String hash = area.getText();
        if (hash != null && !hash.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(hash), null);
            JOptionPane.showMessageDialog(this, "Đã copy kết quả hash vào clipboard!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        fileDropPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        fileDropPanel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 3, 6));
    }

    // kéo thả file
    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            List<File> droppedFiles = (List<File>) dtde.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);
            if (droppedFiles != null && !droppedFiles.isEmpty()) {
                selectedFile = droppedFiles.get(0);
                fileDropLabel.setText(selectedFile.getAbsolutePath());
                fileDropLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
                fileDropLabel.setForeground(Color.BLACK);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi kéo thả file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
        fileDropPanel.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 3, 6));
    }

    // Hash file với progress bar
    private void hashFileWithProgress() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng kéo thả hoặc chọn file để hash.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String algo = (String) algoFileComboBox.getSelectedItem();
        fileProgressBar.setValue(0);
        fileProgressBar.setVisible(true);
        resultFileArea.setText("");
        hashFileButton.setEnabled(false);
        copyFileButton.setEnabled(false);

        SwingWorker<String, Integer> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                long total = selectedFile.length();
                long processed = 0;
                int bufferSize = 8192;
                java.security.MessageDigest md = java.security.MessageDigest.getInstance(algo);
                try (java.io.FileInputStream fis = new java.io.FileInputStream(selectedFile)) {
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        md.update(buffer, 0, bytesRead);
                        processed += bytesRead;
                        int percent = (int) ((processed * 100) / total);
                        setProgress(percent);
                    }
                }
                byte[] hashBytes = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    fileProgressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    String hash = get();
                    resultFileArea.setText(hash);
                } catch (Exception ex) {
                    resultFileArea.setText("");
                    JOptionPane.showMessageDialog(HashPanel.this, "Lỗi khi hash file: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
                fileProgressBar.setVisible(false);
                hashFileButton.setEnabled(true);
                copyFileButton.setEnabled(true);
            }
        };
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                fileProgressBar.setValue((Integer) evt.getNewValue());
            }
        });
        worker.execute();
    }
}