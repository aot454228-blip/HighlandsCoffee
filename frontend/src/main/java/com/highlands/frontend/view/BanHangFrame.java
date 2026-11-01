package com.highlands.frontend.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.highlands.frontend.api.ApiClient;
import com.highlands.frontend.api.ApiException;
import com.highlands.frontend.dto.LoginResponse;
import com.highlands.frontend.dto.SanPhamDto;
import com.highlands.frontend.dto.ToppingDto;

/** Màn hình bán hàng của nhân viên. Đợt này chỉ hiển thị thực đơn và topping. */
public class BanHangFrame extends JFrame {

    private static final Color MAU_CHU_DAO = new Color(0xA6, 0x19, 0x2E);
    private static final NumberFormat TIEN = NumberFormat.getInstance(Locale.of("vi", "VN"));

    /** Gói hai danh sách tải về cùng lúc. */
    private record DuLieuThucDon(List<SanPhamDto> sanPham, List<ToppingDto> topping) { }

    private final ApiClient apiClient = new ApiClient();
    private final LoginResponse nguoiDung;

    private final DefaultTableModel modelSanPham = new DefaultTableModel(
            new String[] { "Mã", "Tên sản phẩm", "Danh mục", "Giá theo size" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // bảng chỉ để xem
        }
    };
    private final JTable tblSanPham = new JTable(modelSanPham);
    private final DefaultListModel<String> modelTopping = new DefaultListModel<>();
    private final JList<String> lstTopping = new JList<>(modelTopping);
    private final JLabel lblTrangThai = new JLabel(" ");
    private final JButton btnTaiLai = new JButton("Tải lại");
    private final JButton btnDangXuat = new JButton("Đăng xuất");

    public BanHangFrame(LoginResponse nguoiDung) {
        super("Highlands Coffee - Bán hàng");
        this.nguoiDung = nguoiDung;
        khoiTaoGiaoDien();
        taiDuLieu();
    }

    private void khoiTaoGiaoDien() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        JLabel lblTieuDe = new JLabel("HIGHLANDS COFFEE - BÁN HÀNG");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(MAU_CHU_DAO);
        pnlTren.add(lblTieuDe, BorderLayout.WEST);

        JPanel pnlNguoiDung = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNguoiDung.add(new JLabel("Xin chào " + nguoiDung.tenDangNhap()
                + " (" + nguoiDung.vaiTro() + ")"));
        pnlNguoiDung.add(btnTaiLai);
        pnlNguoiDung.add(btnDangXuat);
        pnlTren.add(pnlNguoiDung, BorderLayout.EAST);
        add(pnlTren, BorderLayout.NORTH);

        tblSanPham.setRowHeight(28);
        tblSanPham.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblSanPham.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblSanPham.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblSanPham.getColumnModel().getColumn(3).setPreferredWidth(400);
        JScrollPane cuonBang = new JScrollPane(tblSanPham);
        cuonBang.setBorder(BorderFactory.createTitledBorder("Thực đơn"));
        add(cuonBang, BorderLayout.CENTER);

        JScrollPane cuonTopping = new JScrollPane(lstTopping);
        cuonTopping.setBorder(BorderFactory.createTitledBorder("Topping"));
        cuonTopping.setPreferredSize(new Dimension(230, 0));
        add(cuonTopping, BorderLayout.EAST);

        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(4, 14, 8, 14));
        add(lblTrangThai, BorderLayout.SOUTH);

        btnTaiLai.addActionListener(e -> taiDuLieu());
        btnDangXuat.addActionListener(e -> dangXuat());

        setSize(980, 560);
        setLocationRelativeTo(null);
    }

    private void taiDuLieu() {
        btnTaiLai.setEnabled(false);
        hienTrangThai("Đang tải thực đơn...", false);

        // Gọi API ở luồng nền để giao diện không bị đơ
        new SwingWorker<DuLieuThucDon, Void>() {
            @Override
            protected DuLieuThucDon doInBackground() {
                return new DuLieuThucDon(apiClient.layThucDon(), apiClient.layTopping());
            }

            @Override
            protected void done() {
                btnTaiLai.setEnabled(true);
                try {
                    DuLieuThucDon duLieu = get();
                    hienThiThucDon(duLieu);
                    hienTrangThai("Đã tải " + duLieu.sanPham().size() + " sản phẩm, "
                            + duLieu.topping().size() + " topping", false);
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

    private void hienThiThucDon(DuLieuThucDon duLieu) {
        modelSanPham.setRowCount(0);
        for (SanPhamDto sp : duLieu.sanPham()) {
            String giaTheoSize = sp.kichThuoc().stream()
                    .map(k -> k.tenKichThuoc() + ": " + TIEN.format(k.giaBan()))
                    .collect(Collectors.joining("   |   "));
            modelSanPham.addRow(new Object[] {
                    sp.maSanPham(), sp.tenSanPham(), sp.tenDanhMuc(), giaTheoSize });
        }

        modelTopping.clear();
        for (ToppingDto t : duLieu.topping()) {
            modelTopping.addElement(t.tenTopping() + "  (+" + TIEN.format(t.giaBan()) + ")");
        }
    }

    private void dangXuat() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void hienTrangThai(String noiDung, boolean laLoi) {
        lblTrangThai.setForeground(laLoi ? Color.RED : new Color(0x1B, 0x7F, 0x3B));
        lblTrangThai.setText(noiDung);
    }
}