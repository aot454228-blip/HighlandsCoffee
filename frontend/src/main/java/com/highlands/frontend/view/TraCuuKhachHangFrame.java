package com.highlands.frontend.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.highlands.frontend.api.ApiClient;
import com.highlands.frontend.api.ApiException;
import com.highlands.frontend.dto.KhachHangDto;

/** Màn hình tra cứu khách hàng theo tên, số điện thoại hoặc mã khách hàng. */
public class TraCuuKhachHangFrame extends JFrame {

    private static final Color MAU_CHU_DAO = new Color(0xA6, 0x19, 0x2E);
    private static final DateTimeFormatter NGAY_VIET = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ApiClient apiClient;

    private final JTextField txtTuKhoa = new JTextField(24);
    private final JButton btnTim = new JButton("Tìm");
    private final JButton btnDong = new JButton("Đóng");
    private final JLabel lblTrangThai = new JLabel(" ");

    private final DefaultTableModel modelKhach = new DefaultTableModel(
            new String[] { "Mã KH", "Họ tên", "Số điện thoại", "Email", "Ngày sinh", "Giới tính", "Điểm tích lũy" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblKhach = new JTable(modelKhach);

    public TraCuuKhachHangFrame(ApiClient apiClient) {
        super("Highlands Coffee - Tra cứu khách hàng");
        this.apiClient = apiClient;
        khoiTaoGiaoDien();
        timKiem(); // mở lên là hiện sẵn danh sách khách
    }

    private void khoiTaoGiaoDien() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // đóng cửa sổ này không tắt cả chương trình
        setLayout(new BorderLayout(8, 8));

        JLabel lblTieuDe = new JLabel("TRA CỨU KHÁCH HÀNG");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(MAU_CHU_DAO);

        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTimKiem.add(new JLabel("Tên / SĐT / Mã KH:"));
        pnlTimKiem.add(txtTuKhoa);
        pnlTimKiem.add(btnTim);

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        pnlTren.add(lblTieuDe, BorderLayout.WEST);
        pnlTren.add(pnlTimKiem, BorderLayout.EAST);
        add(pnlTren, BorderLayout.NORTH);

        tblKhach.setRowHeight(26);
        tblKhach.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblKhach.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblKhach.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblKhach.getColumnModel().getColumn(3).setPreferredWidth(190);
        tblKhach.getColumnModel().getColumn(4).setPreferredWidth(90);
        tblKhach.getColumnModel().getColumn(5).setPreferredWidth(70);
        tblKhach.getColumnModel().getColumn(6).setPreferredWidth(100);
        JScrollPane cuon = new JScrollPane(tblKhach);
        cuon.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        add(cuon, BorderLayout.CENTER);

        JPanel pnlDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlDong.add(btnDong);
        JPanel pnlDuoi = new JPanel(new BorderLayout(0, 6));
        pnlDuoi.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 14));
        pnlDuoi.add(pnlDong, BorderLayout.EAST);
        pnlDuoi.add(lblTrangThai, BorderLayout.WEST);
        add(pnlDuoi, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnTim); // nhấn Enter = bấm Tìm
        btnTim.addActionListener(e -> timKiem());
        btnDong.addActionListener(e -> dispose());

        setSize(900, 500);
        setLocationRelativeTo(null);
    }

    private void timKiem() {
        final String tuKhoa = txtTuKhoa.getText().trim();
        btnTim.setEnabled(false);
        hienTrangThai("Đang tìm...", false);

        // Gọi API ở luồng nền để giao diện không bị đơ
        new SwingWorker<List<KhachHangDto>, Void>() {
            @Override
            protected List<KhachHangDto> doInBackground() {
                return apiClient.timKhachHang(tuKhoa);
            }

            @Override
            protected void done() {
                btnTim.setEnabled(true);
                try {
                    List<KhachHangDto> ketQua = get();
                    hienThiBang(ketQua);
                    if (ketQua.isEmpty()) {
                        hienTrangThai("Không có khách hàng phù hợp", true);
                    } else {
                        hienTrangThai("Tìm thấy " + ketQua.size() + " khách hàng", false);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable nguyenNhan = e.getCause();
                    String noiDung = (nguyenNhan instanceof ApiException)
                            ? nguyenNhan.getMessage()
                            : "Đã xảy ra lỗi không mong muốn";
                    hienTrangThai(noiDung, true);
                }
            }
        }.execute();
    }

    private void hienThiBang(List<KhachHangDto> cacKhach) {
        modelKhach.setRowCount(0);
        for (KhachHangDto k : cacKhach) {
            modelKhach.addRow(new Object[] {
                    k.maKhachHang(),
                    k.hoTen(),
                    k.soDienThoai(),
                    k.email() == null ? "" : k.email(),
                    dinhDangNgay(k.ngaySinh()),
                    nhanGioiTinh(k.gioiTinh()),
                    k.diemTichLuy() });
        }
    }

    /** Đổi "2002-04-02" thành "02/04/2002". */
    private String dinhDangNgay(String ngay) {
        if (ngay == null || ngay.isBlank()) {
            return "";
        }
        try {
            return LocalDate.parse(ngay).format(NGAY_VIET);
        } catch (DateTimeParseException e) {
            return ngay; // không đúng dạng thì giữ nguyên
        }
    }

    private String nhanGioiTinh(String ma) {
        if ("NAM".equals(ma)) {
            return "Nam";
        }
        if ("NU".equals(ma)) {
            return "Nữ";
        }
        if ("KHAC".equals(ma)) {
            return "Khác";
        }
        return "";
    }

    private void hienTrangThai(String noiDung, boolean laLoi) {
        lblTrangThai.setForeground(laLoi ? Color.RED : new Color(0x1B, 0x7F, 0x3B));
        lblTrangThai.setText(noiDung);
    }
}
