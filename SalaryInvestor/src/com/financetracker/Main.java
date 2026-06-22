package com.financetracker;

import java.awt.Color;

public class Main {

    public static void main(String[] args) {
        System.setProperty("swing.aatext", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("sun.java2d.opengl", "false");

        try {
            UIManager.put("ToolTip.background", Color.DARK_GRAY);

        } catch (Exception ignored) {}

        Thread.setDefaultUncaughtException((t, e) -> {
            System.err.println("Uncaught exception on " + t.getName() + ":");
            e.printStackTrace();
        });
    }
}