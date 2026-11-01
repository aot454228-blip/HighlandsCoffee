package com.highlands.frontend;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.highlands.frontend.view.LoginFrame;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Không đổi được giao diện hệ thống thì dùng giao diện mặc định của Swing
        }
        // Mọi thao tác với giao diện Swing phải chạy trên luồng riêng của Swing
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}