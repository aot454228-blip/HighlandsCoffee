
package com.highlands.frontend;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Highlands Coffee Management");

            frame.setSize(900, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            JLabel title = new JLabel(
                "HIGHLANDS COFFEE",
                SwingConstants.CENTER
            );

            title.setFont(new Font("Arial", Font.BOLD, 30));

            frame.add(title, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
