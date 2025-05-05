package com.atbm.ui;

import javax.swing.*;
import java.awt.*;

public class StyleConstants {
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static final Insets DEFAULT_INSETS = new Insets(10, 10, 10, 10);
    public static final Insets SMALL_INSETS = new Insets(5, 5, 5, 5);

    public static final Dimension BUTTON_SIZE = new Dimension(150, 35);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(0, 35);
    public static final Dimension COMBO_BOX_SIZE = new Dimension(0, 35);

    public static void applyDefaultStyles() {
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("Label.font", LABEL_FONT);
        UIManager.put("TextField.font", TEXT_FONT);
        UIManager.put("TextArea.font", TEXT_FONT);
        UIManager.put("ComboBox.font", TEXT_FONT);
        UIManager.put("Menu.font", MENU_FONT);
        UIManager.put("MenuItem.font", MENU_FONT);
        UIManager.put("List.font", TEXT_FONT);
        UIManager.put("Table.font", TEXT_FONT);

        UIManager.put("TitledBorder.titleColor", Color.BLACK);
        UIManager.put("TitledBorder.titleFont", TITLE_FONT);
    }
}