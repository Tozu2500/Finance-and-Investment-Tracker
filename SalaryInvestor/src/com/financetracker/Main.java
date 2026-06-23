package com.financetracker;

import java.awt.Color;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the desktop version of the application. Opens a login/register window
 * before the main application.
 */

public class Main {

    public static void main(String[] args) {
        System.setProperty("swing.aatext", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("sun.java2d.opengl", "false");

        try {
            UIManager.put("ToolTip.background", Color.DARK_GRAY);

        } catch (Exception ignored) {}

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("Uncaught exception on " + t.getName() + ":");
            e.printStackTrace();
        });

        SwingUtilities.invokeLater(() -> {
            AppContext.get().boot();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}