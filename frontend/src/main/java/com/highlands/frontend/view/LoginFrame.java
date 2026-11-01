package com.highlands.frontend.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.highlands.frontend.api.ApiClient;
import com.highlands.frontend.api.ApiException;
import com.highlands.frontend.dto.LoginResponse;

/** Màn hình đăng nhập. Kế thừa JFrame (cửa sổ chính của Swing). */
public class LoginFrame extends JFrame {

    private static final Color MAU_CHU_DAO = new Color(0xA6, 0x19, 0x2E);

    private final ApiClient apiClient = new ApiClient();

    private final JTextField txtTenDangNhap = new JTextField(20);
    private final JPasswordField txtMatKhau = new JPasswordField(20);
    private final JButton btnDangNhap = new JButton("Đăng nhập");
    private final JLabel lblThongBao = new JLabel(" ");

    public LoginFrame() {
        super("Highlands Coffee - Đăng nhập");
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        JLabel lblTieuDe = new JLabel("HIGHLANDS COFFEE");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTieuDe.setForeground(MAU_CHU_DAO);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(lblTieuDe, c);

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Tên đăng nhập:"), c);
        c.gridx = 1;
        panel.add(txtTenDangNhap, c);

        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Mật khẩu:"), c);
        c.gridx = 1;
        panel.add(txtMatKhau, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        panel.add(lblThongBao, c);

        btnDangNhap.setFocusPainted(false);
        c.gridy = 4;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(btnDangNhap, c);

        setContentPane(panel);
        getRootPane().setDefaultButton(btnDangNhap); // nhấn Enter = bấm nút
        btnDangNhap.addActionListener(e -> xuLyDangNhap());

        pack();
        setResizable(false);
        setLocationRelativeTo(null); // hiện giữa màn hình
    }

    private void xuLyDangNhap() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        char[] matKhau = txtMatKhau.getPassword();
        String matKhauChuoi = new String(matKhau);
        Arrays.fill(matKhau, '\0'); // xóa mảng mật khẩu khỏi bộ nhớ

        if (tenDangNhap.isEmpty() || matKhauChuoi.isEmpty()) {
            hienThongBao("Vui lòng nhập tên đăng nhập và mật khẩu", true);
            return;
        }

        btnDangNhap.setEnabled(false);
        hienThongBao("Đang đăng nhập...", false);

        // Gọi API ở luồng nền để giao diện không bị đơ khi chờ máy chủ
        new SwingWorker<LoginResponse, Void>() {
            @Override
            protected LoginResponse doInBackground() {
                return apiClient.dangNhap(tenDangNhap, matKhauChuoi);
            }

            @Override
            protected void done() {
                btnDangNhap.setEnabled(true);
                try {
                    LoginResponse ketQua = get();
                    hienThongBao("Đăng nhập thành công. Vai trò: " + ketQua.vaiTro(), false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable nguyenNhan = e.getCause();
                    String noiDung = (nguyenNhan instanceof ApiException)
                            ? nguyenNhan.getMessage()
                            : "Đã xảy ra lỗi không mong muốn";
                    hienThongBao(noiDung, true);
                }
            }
        }.execute();
    }

    private void hienThongBao(String noiDung, boolean laLoi) {
        lblThongBao.setForeground(laLoi ? Color.RED : new Color(0x1B, 0x7F, 0x3B));
        lblThongBao.setText(noiDung);
    }
}