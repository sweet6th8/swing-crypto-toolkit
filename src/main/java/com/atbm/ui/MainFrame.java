package com.atbm.ui;

import com.atbm.ui.panels.KeyGenPanel;
import com.atbm.ui.panels.TextEncryptionPanel;
import com.atbm.ui.panels.FileEncryptionPanel;
import com.atbm.ui.panels.KeyListPanel;
import com.atbm.ui.panels.HashPanel;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private JSplitPane splitPane;
    private KeyListPanel keyListPanel;
    private JPanel mainContentPanel; // Panel using CardLayout
    private CardLayout cardLayout;
    private JMenuBar menuBar;

    // Constants for card names
    private static final String HOME_PANEL = "Home";
    private static final String KEY_GEN_PANEL = "Tạo Key";
    private static final String FILE_ENC_PANEL = "File";
    private static final String TEXT_ENC_PANEL = "Văn bản";
    private static final String HASH_PANEL = "Hash";
    private static final String SIGNATURE_PANEL = "Chữ ký điện tử";
    private static final String ABOUT_PANEL = "About";

    public MainFrame() {
        setTitle("Phần mềm mã hoá/giải mã file"); // Updated title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Adjusted size
        setLocationRelativeTo(null);

        // Create Menu Bar
        createMenuBar();
        setJMenuBar(menuBar);

        // Create Key List Panel (Left Panel)
        keyListPanel = new KeyListPanel("./keys");
        keyListPanel.setPreferredSize(new Dimension(250, 0));

        // Create Main Content Panel (Right Panel) using CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.add(new JPanel(), HOME_PANEL); // Add placeholder panels for now
        mainContentPanel.add(new KeyGenPanel(), KEY_GEN_PANEL); // Use actual KeyGenPanel
        mainContentPanel.add(new FileEncryptionPanel(), FILE_ENC_PANEL); // Use actual FileEncryptionPanel
        mainContentPanel.add(new TextEncryptionPanel(), TEXT_ENC_PANEL); // Use actual TextEncryptionPanel
        mainContentPanel.add(new HashPanel(), HASH_PANEL);
        mainContentPanel.add(new JPanel(), SIGNATURE_PANEL);
        mainContentPanel.add(new JPanel(), ABOUT_PANEL);
        // TODO: Replace placeholder JPanels with actual functional panels

        // Create Split Pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, keyListPanel, mainContentPanel);
        splitPane.setDividerLocation(250);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        // Show Home panel initially
        cardLayout.show(mainContentPanel, HOME_PANEL);
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        // --- File Menu --- (Example, adjust based on screenshot) ---
        JMenu homeMenu = new JMenu(HOME_PANEL);
        JMenuItem homeItem = new JMenuItem("Show Home");
        homeItem.addActionListener(e -> cardLayout.show(mainContentPanel, HOME_PANEL));
        homeMenu.add(homeItem);
        menuBar.add(homeMenu);

        // --- Tạo Key Menu ---
        JMenu keyMenu = new JMenu(KEY_GEN_PANEL);
        JMenuItem keyGenItem = new JMenuItem("Mở màn hình tạo khóa");
        keyGenItem.addActionListener(e -> cardLayout.show(mainContentPanel, KEY_GEN_PANEL));
        keyMenu.add(keyGenItem);
        menuBar.add(keyMenu);

        // --- File Menu ---
        JMenu fileMenu = new JMenu(FILE_ENC_PANEL);
        JMenu fileEncryptMenu = new JMenu("Đối xứng");
        JMenuItem fileEncryptSymItem = new JMenuItem("Mã hóa");
        JMenuItem fileDecryptSymItem = new JMenuItem("Giải mã");
        fileEncryptSymItem.addActionListener(e -> cardLayout.show(mainContentPanel, FILE_ENC_PANEL)); // Show file panel
        fileDecryptSymItem.addActionListener(e -> cardLayout.show(mainContentPanel, FILE_ENC_PANEL)); // Show file panel
        fileEncryptMenu.add(fileEncryptSymItem);
        fileEncryptMenu.add(fileDecryptSymItem);

        JMenu fileEncryptAsymMenu = new JMenu("Bất đối xứng");
        JMenuItem fileEncryptAsymItem = new JMenuItem("Mã hóa");
        JMenuItem fileDecryptAsymItem = new JMenuItem("Giải mã");
        fileEncryptAsymItem.addActionListener(e -> cardLayout.show(mainContentPanel, FILE_ENC_PANEL));
        fileDecryptAsymItem.addActionListener(e -> cardLayout.show(mainContentPanel, FILE_ENC_PANEL));
        fileEncryptAsymMenu.add(fileEncryptAsymItem);
        fileEncryptAsymMenu.add(fileDecryptAsymItem);

        JMenu fileEncryptTradMenu = new JMenu("Cổ điển");
        JMenuItem fileEncryptTradItem = new JMenuItem("Mã hóa");
        JMenuItem fileDecryptTradItem = new JMenuItem("Giải mã");
        fileEncryptTradItem.addActionListener(e -> cardLayout.show(mainContentPanel, FILE_ENC_PANEL));
        fileDecryptTradItem.addActionListener(e -> cardLayout.show(mainContentPanel, FILE_ENC_PANEL));
        fileEncryptTradMenu.add(fileEncryptTradItem);
        fileEncryptTradMenu.add(fileDecryptTradItem);

        fileMenu.add(fileEncryptMenu);
        fileMenu.add(fileEncryptAsymMenu);
        fileMenu.add(fileEncryptTradMenu);
        menuBar.add(fileMenu);

        // --- Văn bản Menu ---
        JMenu textMenu = new JMenu(TEXT_ENC_PANEL);
        JMenu textEncryptMenu = new JMenu("Đối xứng");
        JMenuItem textEncryptSymItem = new JMenuItem("Mã hóa");
        JMenuItem textDecryptSymItem = new JMenuItem("Giải mã");
        textEncryptSymItem.addActionListener(e -> cardLayout.show(mainContentPanel, TEXT_ENC_PANEL)); // Show text panel
        textDecryptSymItem.addActionListener(e -> cardLayout.show(mainContentPanel, TEXT_ENC_PANEL)); // Show text panel
        textEncryptMenu.add(textEncryptSymItem);
        textEncryptMenu.add(textDecryptSymItem);

        JMenu textEncryptAsymMenu = new JMenu("Bất đối xứng");
        JMenuItem textEncryptAsymItem = new JMenuItem("Mã hóa");
        JMenuItem textDecryptAsymItem = new JMenuItem("Giải mã");
        textEncryptAsymItem.addActionListener(e -> cardLayout.show(mainContentPanel, TEXT_ENC_PANEL));
        textDecryptAsymItem.addActionListener(e -> cardLayout.show(mainContentPanel, TEXT_ENC_PANEL));
        textEncryptAsymMenu.add(textEncryptAsymItem);
        textEncryptAsymMenu.add(textDecryptAsymItem);

        JMenu textEncryptTradMenu = new JMenu("Cổ điển");
        JMenuItem textEncryptTradItem = new JMenuItem("Mã hóa");
        JMenuItem textDecryptTradItem = new JMenuItem("Giải mã");
        textEncryptTradItem.addActionListener(e -> cardLayout.show(mainContentPanel, TEXT_ENC_PANEL));
        textDecryptTradItem.addActionListener(e -> cardLayout.show(mainContentPanel, TEXT_ENC_PANEL));
        textEncryptTradMenu.add(textEncryptTradItem);
        textEncryptTradMenu.add(textDecryptTradItem);

        textMenu.add(textEncryptMenu);
        textMenu.add(textEncryptAsymMenu);
        textMenu.add(textEncryptTradMenu);
        menuBar.add(textMenu);

        // --- Hash Menu ---
        JMenu hashMenu = new JMenu(HASH_PANEL);
        JMenuItem hashItem = new JMenuItem("Mở màn hình Hash");
        hashItem.addActionListener(e -> cardLayout.show(mainContentPanel, HASH_PANEL));
        hashMenu.add(hashItem);
        menuBar.add(hashMenu);

        // --- Chữ ký điện tử Menu ---
        JMenu signatureMenu = new JMenu(SIGNATURE_PANEL);
        JMenuItem signatureItem = new JMenuItem("Mở màn hình Chữ ký");
        signatureItem.addActionListener(e -> cardLayout.show(mainContentPanel, SIGNATURE_PANEL));
        signatureMenu.add(signatureItem);
        menuBar.add(signatureMenu);

        // --- About Menu ---
        JMenu aboutMenu = new JMenu(ABOUT_PANEL);
        JMenuItem aboutItem = new JMenuItem("Thông tin");
        aboutItem.addActionListener(e -> cardLayout.show(mainContentPanel, ABOUT_PANEL));
        aboutMenu.add(aboutItem);
        menuBar.add(aboutMenu);
    }

    public static void main(String[] args) {
        // Set Look and Feel (Optional, for better appearance)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}