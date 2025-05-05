package com.atbm.ui.panels;

import com.atbm.ui.StyleConstants;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

// Class này là panel hiển thị danh sách khóa
public class KeyListPanel extends JPanel {

    private JList<File> keyList;
    private DefaultListModel<File> listModel;
    private final File keyDirectory;

    public KeyListPanel(String keyDirPath) {
        keyDirectory = new File(keyDirPath);
        ensureKeyDirectoryExists();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Danh sách khóa"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        listModel = new DefaultListModel<>();
        keyList = new JList<>(listModel);
        keyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keyList.setCellRenderer(new KeyFileRenderer());
        keyList.setFont(StyleConstants.TEXT_FONT);
        keyList.setFixedCellHeight(35); // Increased row height

        JScrollPane scrollPane = new JScrollPane(keyList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(scrollPane, BorderLayout.CENTER);

        loadKeyFiles();
    }

    // Đảm bảo thư mục khóa tồn tại
    private void ensureKeyDirectoryExists() {
        if (!keyDirectory.exists()) {
            if (keyDirectory.mkdirs()) {
                System.out.println("Created key directory: " + keyDirectory.getAbsolutePath());
            } else {
                System.err.println("Failed to create key directory: " + keyDirectory.getAbsolutePath());
            }
        } else if (!keyDirectory.isDirectory()) {
            System.err.println("Key path exists but is not a directory: " + keyDirectory.getAbsolutePath());
        }
    }

    // Load file key
    public void loadKeyFiles() {
        listModel.clear();
        if (keyDirectory.isDirectory()) {
            File[] files = keyDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".key") ||
                            name.toLowerCase().endsWith(".pub") ||
                            name.toLowerCase().endsWith(".pri");
                }
            });

            if (files != null) {
                // Sort files alphabetically
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (File file : files) {
                    listModel.addElement(file);
                }
            }
        }
    }

    public File getSelectedKeyFile() {
        return keyList.getSelectedValue();
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        keyList.addListSelectionListener(listener);
    }

    public void refreshKeyList() {
        loadKeyFiles();
    }

    // Hiển thị tên file
    private static class KeyFileRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                setText(((File) value).getName());
            }
            return c;
        }
    }

    // Test độc lập
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("KeyListPanel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        KeyListPanel keyListPanel = new KeyListPanel("./test_keys"); // Use a test directory

        // Example of listening to selection changes
        keyListPanel.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Only react when selection is final
                File selectedFile = keyListPanel.getSelectedKeyFile();
                if (selectedFile != null) {
                    System.out.println("Selected key file: " + selectedFile.getName());
                } else {
                    System.out.println("Selection cleared.");
                }
            }
        });

        frame.getContentPane().add(keyListPanel);
        frame.setPreferredSize(new Dimension(250, 400));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}