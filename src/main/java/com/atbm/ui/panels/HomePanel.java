package com.atbm.ui.panels;

import com.atbm.ui.StyleConstants;
import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {
        public HomePanel() {
                setLayout(new BorderLayout());
                setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JPanel contentPanel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = StyleConstants.DEFAULT_INSETS;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;

                gbc.gridx = 0;
                gbc.gridy = 0;
                JLabel welcomeLabel = new JLabel("Chào mừng đến với Phần mềm Mã hóa/Giải mã");
                welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                contentPanel.add(welcomeLabel, gbc);

                gbc.gridy++;
                JTextArea description = new JTextArea(
                                "Phần mềm này cung cấp các công cụ mạnh mẽ để mã hóa và giải mã dữ liệu " +
                                                "sử dụng nhiều thuật toán khác nhau. Bạn có thể:\n\n" +
                                                "• Tạo và quản lý khóa mã hóa\n" +
                                                "• Mã hóa/giải mã văn bản\n" +
                                                "• Mã hóa/giải mã file\n" +
                                                "• Tạo giá trị hash cho dữ liệu");
                description.setFont(StyleConstants.TEXT_FONT);
                description.setEditable(false);
                description.setLineWrap(true);
                description.setWrapStyleWord(true);
                description.setBackground(getBackground());
                description.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                contentPanel.add(description, gbc);

                // Add usage instructions
                gbc.gridy++;
                JPanel instructionsPanel = new JPanel(new GridBagLayout());
                instructionsPanel.setBorder(BorderFactory.createTitledBorder("Hướng dẫn sử dụng"));
                GridBagConstraints gbc2 = new GridBagConstraints();
                gbc2.insets = StyleConstants.SMALL_INSETS;
                gbc2.fill = GridBagConstraints.HORIZONTAL;
                gbc2.weightx = 1.0;

                addInstruction(instructionsPanel, gbc2, 0, "1. Tạo khóa",
                                "Sử dụng menu 'Tạo khóa' để tạo khóa mã hóa mới. Chọn thuật toán và kích thước khóa phù hợp.");
                addInstruction(instructionsPanel, gbc2, 1, "2. Mã hóa/Giải mã",
                                "Chọn 'Văn bản' hoặc 'File' từ menu để thực hiện mã hóa/giải mã.");
                addInstruction(instructionsPanel, gbc2, 2, "3. Hash",
                                "Sử dụng menu 'Hash' để tạo giá trị hash cho dữ liệu của bạn.");
                addInstruction(instructionsPanel, gbc2, 3, "4. Quản lý khóa",
                                "Danh sách khóa bên trái hiển thị tất cả các khóa đã tạo. Chọn khóa để sử dụng.");

                contentPanel.add(instructionsPanel, gbc);

                gbc.gridy++;
                gbc.anchor = GridBagConstraints.SOUTH;
                JLabel versionLabel = new JLabel("Phiên bản 1.0.0");
                versionLabel.setFont(StyleConstants.LABEL_FONT);
                versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
                contentPanel.add(versionLabel, gbc);

                add(contentPanel, BorderLayout.CENTER);
        }

        private void addInstruction(JPanel panel, GridBagConstraints gbc, int row, String title, String description) {
                gbc.gridy = row;
                gbc.gridx = 0;
                gbc.weightx = 0.2;
                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(StyleConstants.TITLE_FONT);
                panel.add(titleLabel, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.8;
                JTextArea descArea = new JTextArea(description);
                descArea.setFont(StyleConstants.TEXT_FONT);
                descArea.setEditable(false);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);
                descArea.setBackground(panel.getBackground());
                descArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                panel.add(descArea, gbc);
        }
}