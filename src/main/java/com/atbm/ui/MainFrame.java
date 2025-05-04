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
    private static final String KEY_GEN_PANEL = "Tạo khóa";
    private static final String FILE_ENC_PANEL = "File";
    private static final String TEXT_ENC_PANEL = "Văn bản";
    private static final String HASH_PANEL = "Hash";

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
        mainContentPanel.add(new JPanel(), HOME_PANEL);
        mainContentPanel.add(new KeyGenPanel(), KEY_GEN_PANEL);
        mainContentPanel.add(new FileEncryptionPanel(), FILE_ENC_PANEL);
        mainContentPanel.add(new TextEncryptionPanel(), TEXT_ENC_PANEL);
        mainContentPanel.add(new HashPanel(), HASH_PANEL);

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

        // --- Tạo khóa Menu ---
        JMenu keyGenMenu = new JMenu(KEY_GEN_PANEL);
        JMenuItem keyGenItem = new JMenuItem("Mở màn hình Tạo khóa");
        keyGenItem.addActionListener(e -> cardLayout.show(mainContentPanel, KEY_GEN_PANEL));
        keyGenMenu.add(keyGenItem);
        menuBar.add(keyGenMenu);

        // --- File Menu ---
        JMenu fileMenu = new JMenu(FILE_ENC_PANEL);
        JMenuItem fileSymItem = new JMenuItem("Đối xứng");
        fileSymItem.addActionListener(e -> {
            ((FileEncryptionPanel) mainContentPanel.getComponent(2)).setAlgorithmType("Symmetric");
            cardLayout.show(mainContentPanel, FILE_ENC_PANEL);
        });
        fileMenu.add(fileSymItem);
        JMenuItem fileAsymItem = new JMenuItem("Bất đối xứng");
        fileAsymItem.addActionListener(e -> {
            ((FileEncryptionPanel) mainContentPanel.getComponent(2)).setAlgorithmType("Asymmetric");
            cardLayout.show(mainContentPanel, FILE_ENC_PANEL);
        });
        fileMenu.add(fileAsymItem);
        menuBar.add(fileMenu);

        // --- Văn bản Menu ---
        JMenu textMenu = new JMenu(TEXT_ENC_PANEL);
        JMenuItem textSymItem = new JMenuItem("Đối xứng");
        textSymItem.addActionListener(e -> {
            ((TextEncryptionPanel) mainContentPanel.getComponent(3)).setAlgorithmType("Symmetric");
            cardLayout.show(mainContentPanel, TEXT_ENC_PANEL);
        });
        textMenu.add(textSymItem);
        JMenuItem textAsymItem = new JMenuItem("Bất đối xứng");
        textAsymItem.addActionListener(e -> {
            ((TextEncryptionPanel) mainContentPanel.getComponent(3)).setAlgorithmType("Asymmetric");
            cardLayout.show(mainContentPanel, TEXT_ENC_PANEL);
        });
        textMenu.add(textAsymItem);
        JMenuItem textTradItem = new JMenuItem("Cổ điển");
        textTradItem.addActionListener(e -> {
            ((TextEncryptionPanel) mainContentPanel.getComponent(3)).setAlgorithmType("Traditional");
            cardLayout.show(mainContentPanel, TEXT_ENC_PANEL);
        });
        textMenu.add(textTradItem);
        menuBar.add(textMenu);

        // --- Hash Menu ---
        JMenu hashMenu = new JMenu(HASH_PANEL);
        JMenuItem hashItem = new JMenuItem("Mở màn hình Hash");
        hashItem.addActionListener(e -> cardLayout.show(mainContentPanel, HASH_PANEL));
        hashMenu.add(hashItem);
        menuBar.add(hashMenu);
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