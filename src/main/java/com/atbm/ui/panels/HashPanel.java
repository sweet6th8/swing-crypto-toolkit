package com.atbm.ui.panels;

import com.atbm.core.hash.HashAlgorithm;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class HashPanel extends JPanel {
    private JComboBox<String> algorithmComboBox;
    private JTextArea inputTextArea;
    private JButton hashTextButton;
    private JLabel fileLabel;
    private JButton chooseFileButton;
    private JButton hashFileButton;
    private JTextArea resultArea;
    private File selectedFile;

    public HashPanel() {
        setLayout(new BorderLayout(10, 10));

        // Top: Chọn thuật toán
        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algoPanel.setBorder(new TitledBorder("Chọn thuật toán hash"));
        algorithmComboBox = new JComboBox<>(HashAlgorithm.getSupportedAlgorithms());
        algoPanel.add(new JLabel("Thuật toán:"));
        algoPanel.add(algorithmComboBox);
        add(algoPanel, BorderLayout.NORTH);

        // Center: Nhập text hoặc chọn file
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Hash text
        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setBorder(new TitledBorder("Hash Text"));
        inputTextArea = new JTextArea(4, 40);
        JScrollPane textScroll = new JScrollPane(inputTextArea);
        textPanel.add(textScroll, BorderLayout.CENTER);
        hashTextButton = new JButton("Hash Text");
        textPanel.add(hashTextButton, BorderLayout.EAST);
        centerPanel.add(textPanel);

        // Hash file
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.setBorder(new TitledBorder("Hash File"));
        fileLabel = new JLabel("Chưa chọn file");
        chooseFileButton = new JButton("Chọn File...");
        hashFileButton = new JButton("Hash File");
        JPanel fileButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileButtonPanel.add(chooseFileButton);
        fileButtonPanel.add(hashFileButton);
        filePanel.add(fileLabel, BorderLayout.CENTER);
        filePanel.add(fileButtonPanel, BorderLayout.EAST);
        centerPanel.add(filePanel);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom: Kết quả
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(new TitledBorder("Kết quả hash"));
        resultArea = new JTextArea(3, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultPanel.add(resultScroll, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);

        // Action listeners
        hashTextButton.addActionListener(e -> hashText());
        chooseFileButton.addActionListener(e -> chooseFile());
        hashFileButton.addActionListener(e -> hashFile());
    }

    private void hashText() {
        String text = inputTextArea.getText();
        String algo = (String) algorithmComboBox.getSelectedItem();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập text để hash.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String hash = HashAlgorithm.hashText(text, algo);
            resultArea.setText(hash);
        } catch (Exception ex) {
            resultArea.setText("");
            JOptionPane.showMessageDialog(this, "Lỗi khi hash text: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileLabel.setText(selectedFile.getAbsolutePath());
        }
    }

    private void hashFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file để hash.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String algo = (String) algorithmComboBox.getSelectedItem();
        try {
            String hash = HashAlgorithm.hashFile(selectedFile, algo);
            resultArea.setText(hash);
        } catch (Exception ex) {
            resultArea.setText("");
            JOptionPane.showMessageDialog(this, "Lỗi khi hash file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}