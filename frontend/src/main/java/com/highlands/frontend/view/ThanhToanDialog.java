package com.highlands.frontend.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.highlands.frontend.api.ApiClient;
import com.highlands.frontend.api.ApiException;
import com.highlands.frontend.dto.DonHangDto;
import com.highlands.frontend.dto.HoaDonDto;
import com.highlands.frontend.dto.LoginResponse;
import com.highlands.frontend.model.MonTrongGio;
import com.highlands.frontend.util.TienTe;

/** Hộp thoại thanh toán: tạo đơn rồi thu tiền (tiền mặt hoặc chuyển khoản). */
public class ThanhToanDialog extends JDialog {

    private static final Color MAU_CHU_DAO = new Color(0xA6, 0x19, 0x2E);

    private final ApiClient apiClient;
    private final LoginResponse nguoiDung;
    private final List<MonTrongGio> cacMon;
    private final BigDecimal tongTien;

    private final JComboBox<String> cboHinhThuc = new JComboBox<>(new String[] { "Tại chỗ", "Mang đi" });
    private final JRadioButton radTienMat = new JRadioButton("Tiền mặt", true);
    private final JRadioButton radChuyenKhoan = new JRadioButton("Chuyển khoản");
    private final JTextField txtKhachDua = new JTextField(14);
    private final JTextField txtMaGiaoDich = new JTextField(14);
    private final JLabel lblTienThua = new JLabel("Tiền thừa: -");
    private final JLabel lblThongBao = new JLabel(" ");
    private final JButton btnXacNhan = new JButton("Xác nhận thanh toán");
    private final JButton btnHuy = new JButton("Hủy");

    // Có giá trị sau khi tạo đơn thành công (để thử thanh toán lại mà không tạo thêm đơn)
    private Long donHangId;
    // Có giá trị sau khi thanh toán thành công
    private HoaDonDto ketQua;

    public ThanhToanDialog(Frame chu, ApiClient apiClient, LoginResponse nguoiDung,
            List<MonTrongGio> cacMon, BigDecimal tongTien) {
        super(chu, "Thanh toán", true);
        this.apiClient = apiClient;
        this.nguoiDung = nguoiDung;
        this.cacMon = new ArrayList<>(cacMon); // lấy bản sao để giỏ hàng bên ngoài đổi cũng không ảnh hưởng
        this.tongTien = tongTien;
        khoiTaoGiaoDien();
    }

    /** Hóa đơn sau khi thanh toán thành công; null nếu người dùng hủy. */
    public HoaDonDto getKetQua() {
        return ketQua;
    }

    private void khoiTaoGiaoDien() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        ButtonGroup nhomPhuongThuc = new ButtonGroup();
        nhomPhuongThuc.add(radTienMat);
        nhomPhuongThuc.add(radChuyenKhoan);
        JPanel pnlPhuongThuc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlPhuongThuc.add(radTienMat);
        pnlPhuongThuc.add(radChuyenKhoan);

        JLabel lblTong = new JLabel(TienTe.dinhDang(tongTien) + " đ");
        lblTong.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTong.setForeground(MAU_CHU_DAO);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        themDong(panel, c, 0, "Tổng tiền cần thanh toán:", lblTong);
        themDong(panel, c, 1, "Hình thức phục vụ:", cboHinhThuc);
        themDong(panel, c, 2, "Phương thức thanh toán:", pnlPhuongThuc);
        themDong(panel, c, 3, "Khách đưa (đ):", txtKhachDua);
        c.gridx = 1; c.gridy = 4; c.gridwidth = 1;
        panel.add(lblTienThua, c);
        themDong(panel, c, 5, "Mã giao dịch:", txtMaGiaoDich);

        c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
        panel.add(lblThongBao, c);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.add(btnHuy);
        pnlNut.add(btnXacNhan);
        c.gridy = 7;
        c.anchor = GridBagConstraints.EAST;
        panel.add(pnlNut, c);

        setContentPane(panel);
        getRootPane().setDefaultButton(btnXacNhan);

        radTienMat.addActionListener(e -> capNhatTrangThaiO());
        radChuyenKhoan.addActionListener(e -> capNhatTrangThaiO());
        txtKhachDua.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                capNhatTienThua();
            }
        });
        btnXacNhan.addActionListener(e -> xacNhan());
        btnHuy.addActionListener(e -> huy());

        capNhatTrangThaiO();
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void themDong(JPanel panel, GridBagConstraints c, int dong, String nhan, Component thanhPhan) {
        c.gridx = 0; c.gridy = dong; c.gridwidth = 1;
        panel.add(new JLabel(nhan), c);
        c.gridx = 1;
        panel.add(thanhPhan, c);
    }

    /** Tiền mặt thì nhập "khách đưa", chuyển khoản thì nhập "mã giao dịch". */
    private void capNhatTrangThaiO() {
        boolean tienMat = radTienMat.isSelected();
        txtKhachDua.setEnabled(tienMat);
        lblTienThua.setEnabled(tienMat);
        txtMaGiaoDich.setEnabled(!tienMat);
    }

    /** Chỉ lấy các chữ số, nên gõ "50.000" hay "50000" đều được. */
    private BigDecimal docSoTien(String chu) {
        String chiSo = chu.replaceAll("[^0-9]", "");
        if (chiSo.isEmpty() || chiSo.length() > 12) {
            return null;
        }
        return new BigDecimal(chiSo);
    }

    private void capNhatTienThua() {
        BigDecimal khachDua = docSoTien(txtKhachDua.getText());
        if (khachDua == null) {
            lblTienThua.setText("Tiền thừa: -");
            return;
        }
        BigDecimal chenhLech = khachDua.subtract(tongTien);
        if (chenhLech.signum() < 0) {
            lblTienThua.setText("Còn thiếu: " + TienTe.dinhDang(chenhLech.negate()) + " đ");
        } else {
            lblTienThua.setText("Tiền thừa: " + TienTe.dinhDang(chenhLech) + " đ");
        }
    }

    private void xacNhan() {
        boolean tienMat = radTienMat.isSelected();
        BigDecimal soTienKhachDua = null;
        String maGiaoDich = null;

        if (tienMat) {
            soTienKhachDua = docSoTien(txtKhachDua.getText());
            if (soTienKhachDua == null || soTienKhachDua.compareTo(tongTien) < 0) {
                hienThongBao("Số tiền khách đưa chưa đủ", true);
                return;
            }
        } else {
            maGiaoDich = txtMaGiaoDich.getText().trim();
            if (maGiaoDich.isEmpty()) {
                hienThongBao("Vui lòng nhập mã giao dịch", true);
                return;
            }
        }

        // Bản "final" để dùng được bên trong SwingWorker
        final String hinhThucPhucVu = cboHinhThuc.getSelectedIndex() == 0 ? "TAI_CHO" : "MANG_DI";
        final String phuongThuc = tienMat ? "TIEN_MAT" : "CHUYEN_KHOAN";
        final BigDecimal khachDuaCuoi = soTienKhachDua;
        final String maGiaoDichCuoi = maGiaoDich;

        setDangXuLy(true);
        hienThongBao("Đang xử lý...", false);

        // Gọi API ở luồng nền để giao diện không bị đơ
        new SwingWorker<HoaDonDto, Void>() {
            @Override
            protected HoaDonDto doInBackground() {
                if (donHangId == null) {
                    DonHangDto don = apiClient.taoDonTaiQuay(nguoiDung.id(), hinhThucPhucVu, null, cacMon);
                    donHangId = don.id();
                }
                return apiClient.thanhToan(donHangId, nguoiDung.id(), phuongThuc,
                        khachDuaCuoi, maGiaoDichCuoi);
            }

            @Override
            protected void done() {
                setDangXuLy(false);
                try {
                    ketQua = get();
                    dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable nguyenNhan = e.getCause();
                    String noiDung = (nguyenNhan instanceof ApiException)
                            ? nguyenNhan.getMessage()
                            : "Đã xảy ra lỗi không mong muốn";
                    if (donHangId != null) {
                        noiDung += " (đơn đã được tạo, bấm Xác nhận để thanh toán lại)";
                    }
                    hienThongBao(noiDung, true);
                }
            }
        }.execute();
    }

    private void huy() {
        if (donHangId != null) {
            JOptionPane.showMessageDialog(this,
                    "Đơn hàng đã được tạo trên hệ thống nhưng chưa thanh toán.",
                    "Lưu ý", JOptionPane.INFORMATION_MESSAGE);
        }
        dispose();
    }

    private void setDangXuLy(boolean dangXuLy) {
        btnXacNhan.setEnabled(!dangXuLy);
        btnHuy.setEnabled(!dangXuLy);
    }

    private void hienThongBao(String noiDung, boolean laLoi) {
        lblThongBao.setForeground(laLoi ? Color.RED : new Color(0x1B, 0x7F, 0x3B));
        // Dùng HTML để thông báo dài tự xuống dòng thay vì làm rộng hộp thoại
        lblThongBao.setText("<html><body style='width:330px'>" + noiDung + "</body></html>");
    }
}
