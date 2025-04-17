package ui;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class KeyListPanel extends JPanel {

    private JList<File> keyList;
    private DefaultListModel<File> listModel;
    private final File keyDirectory;
    private JButton refreshButton;

    public KeyListPanel(String keyDirPath) {
        keyDirectory = new File(keyDirPath);
        ensureKeyDirectoryExists();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Danh sách khóa"));

        listModel = new DefaultListModel<>();
        keyList = new JList<>(listModel);
        keyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keyList.setCellRenderer(new KeyFileRenderer()); // Custom renderer for better display

        JScrollPane scrollPane = new JScrollPane(keyList);
        add(scrollPane, BorderLayout.CENTER);

        // Add a refresh button at the bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Làm mới");
        refreshButton.addActionListener(e -> loadKeyFiles());
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadKeyFiles();
    }

    private void ensureKeyDirectoryExists() {
        if (!keyDirectory.exists()) {
            if (keyDirectory.mkdirs()) {
                System.out.println("Created key directory: " + keyDirectory.getAbsolutePath());
            } else {
                System.err.println("Failed to create key directory: " + keyDirectory.getAbsolutePath());
                // Handle error appropriately, maybe show a dialog
            }
        } else if (!keyDirectory.isDirectory()) {
            System.err.println("Key path exists but is not a directory: " + keyDirectory.getAbsolutePath());
            // Handle error
        }
    }

    public void loadKeyFiles() {
        listModel.clear();
        if (keyDirectory.isDirectory()) {
            // Filter for .key, .pub, .pri files
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

    // Custom cell renderer to show only the filename
    private static class KeyFileRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // Call superclass to get default formatting
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                setText(((File) value).getName()); // Display only the file name
                // Set icon based on file type (optional)
                String name = ((File) value).getName().toLowerCase();
                if (name.endsWith(".key")) {
                    // setIcon(your_symmetric_key_icon);
                } else if (name.endsWith(".pub")) {
                    // setIcon(your_public_key_icon);
                } else if (name.endsWith(".pri")) {
                    // setIcon(your_private_key_icon);
                }
            }
            return c;
        }
    }

    // Main method for testing this panel independently
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