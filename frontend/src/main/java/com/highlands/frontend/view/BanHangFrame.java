package com.highlands.frontend.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.highlands.frontend.api.ApiClient;
import com.highlands.frontend.api.ApiException;
import com.highlands.frontend.dto.HoaDonDto;
import com.highlands.frontend.dto.LoginResponse;
import com.highlands.frontend.dto.SanPhamDto;
import com.highlands.frontend.dto.ToppingDto;
import com.highlands.frontend.model.GioHang;
import com.highlands.frontend.model.MonTrongGio;
import com.highlands.frontend.util.TienTe;

/** Màn hình bán hàng của nhân viên: thực đơn ở trên, giỏ hàng ở dưới. */
public class BanHangFrame extends JFrame {

    private static final Color MAU_CHU_DAO = new Color(0xA6, 0x19, 0x2E);

    /** Gói hai danh sách tải về cùng lúc. */
    private record DuLieuThucDon(List<SanPhamDto> sanPham, List<ToppingDto> topping) { }

    private final ApiClient apiClient = new ApiClient();
    private final LoginResponse nguoiDung;
    private final GioHang gioHang = new GioHang();
    private DonHangFrame cuaSoDonHang; // cửa sổ xử lý đơn, mở khi bấm nút "Đơn hàng"
    private TraCuuKhachHangFrame cuaSoKhachHang; // cửa sổ tra cứu khách, mở khi bấm nút "Khách hàng"

    // Dòng thứ i của bảng thực đơn tương ứng phần tử thứ i của dsSanPham
    private List<SanPhamDto> dsSanPham = new ArrayList<>();
    private List<ToppingDto> dsTopping = new ArrayList<>();

    private final DefaultTableModel modelSanPham = new DefaultTableModel(
            new String[] { "Mã", "Tên sản phẩm", "Danh mục", "Giá theo size" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblSanPham = new JTable(modelSanPham);

    private final DefaultTableModel modelGio = new DefaultTableModel(
            new String[] { "Sản phẩm", "Size", "SL", "Topping", "Tùy chọn", "Thành tiền" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblGio = new JTable(modelGio);

    private final JLabel lblTong = new JLabel("Tổng tiền hàng: 0 đ");
    private final JLabel lblTrangThai = new JLabel(" ");
    private final JButton btnTaiLai = new JButton("Tải lại");
    private final JButton btnDangXuat = new JButton("Đăng xuất");
    private final JButton btnThemVaoGio = new JButton("Thêm vào giỏ");
    private final JButton btnXoaDong = new JButton("Xóa dòng");
    private final JButton btnLamMoiGio = new JButton("Xóa cả giỏ");
    private final JButton btnThanhToan = new JButton("Thanh toán");
    private final JButton btnDonHang = new JButton("Đơn hàng");
    private final JButton btnKhachHang = new JButton("Khách hàng");

    public BanHangFrame(LoginResponse nguoiDung) {
        super("Highlands Coffee - Bán hàng");
        this.nguoiDung = nguoiDung;
        khoiTaoGiaoDien();
        taiDuLieu();
    }

    private void khoiTaoGiaoDien() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // --- Thanh trên: tiêu đề và thông tin người dùng ---
        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        JLabel lblTieuDe = new JLabel("HIGHLANDS COFFEE - BÁN HÀNG");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(MAU_CHU_DAO);
        pnlTren.add(lblTieuDe, BorderLayout.WEST);

        JPanel pnlNguoiDung = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNguoiDung.add(new JLabel("Xin chào " + nguoiDung.tenDangNhap()
                + " (" + nguoiDung.vaiTro() + ")"));
        pnlNguoiDung.add(btnKhachHang);
        pnlNguoiDung.add(btnDonHang);
        pnlNguoiDung.add(btnTaiLai);
        pnlNguoiDung.add(btnDangXuat);
        pnlTren.add(pnlNguoiDung, BorderLayout.EAST);
        add(pnlTren, BorderLayout.NORTH);

        // --- Phần thực đơn ---
        tblSanPham.setRowHeight(28);
        tblSanPham.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblSanPham.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblSanPham.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblSanPham.getColumnModel().getColumn(3).setPreferredWidth(400);
        tblSanPham.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    themVaoGio(); // bấm đúp để chọn món
                }
            }
        });
        JScrollPane cuonThucDon = new JScrollPane(tblSanPham);
        cuonThucDon.setBorder(BorderFactory.createTitledBorder("Thực đơn (bấm đúp để chọn món)"));

        JPanel pnlThucDon = new JPanel(new BorderLayout(0, 6));
        pnlThucDon.add(cuonThucDon, BorderLayout.CENTER);
        JPanel pnlNutThucDon = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlNutThucDon.add(btnThemVaoGio);
        pnlThucDon.add(pnlNutThucDon, BorderLayout.SOUTH);

        // --- Phần giỏ hàng ---
        tblGio.setRowHeight(26);
        tblGio.getColumnModel().getColumn(0).setPreferredWidth(150);
        tblGio.getColumnModel().getColumn(1).setPreferredWidth(70);
        tblGio.getColumnModel().getColumn(2).setPreferredWidth(40);
        tblGio.getColumnModel().getColumn(3).setPreferredWidth(200);
        tblGio.getColumnModel().getColumn(4).setPreferredWidth(200);
        tblGio.getColumnModel().getColumn(5).setPreferredWidth(100);
        JScrollPane cuonGio = new JScrollPane(tblGio);
        cuonGio.setBorder(BorderFactory.createTitledBorder("Giỏ hàng"));

        lblTong.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTong.setForeground(MAU_CHU_DAO);
        JPanel pnlNutGio = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNutGio.add(btnXoaDong);
        pnlNutGio.add(btnLamMoiGio);
        JPanel pnlDuoiGio = new JPanel(new BorderLayout());
        pnlDuoiGio.add(pnlNutGio, BorderLayout.WEST);
        btnThanhToan.setFont(btnThanhToan.getFont().deriveFont(Font.BOLD));
        JPanel pnlPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlPhai.add(lblTong);
        pnlPhai.add(btnThanhToan);
        pnlDuoiGio.add(pnlPhai, BorderLayout.EAST);

        JPanel pnlGio = new JPanel(new BorderLayout(0, 6));
        pnlGio.add(cuonGio, BorderLayout.CENTER);
        pnlGio.add(pnlDuoiGio, BorderLayout.SOUTH);

        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlThucDon, pnlGio);
        chia.setResizeWeight(0.5);
        chia.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        add(chia, BorderLayout.CENTER);

        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(4, 14, 8, 14));
        add(lblTrangThai, BorderLayout.SOUTH);

        btnTaiLai.addActionListener(e -> taiDuLieu());
        btnDangXuat.addActionListener(e -> dangXuat());
        btnThemVaoGio.addActionListener(e -> themVaoGio());
        btnXoaDong.addActionListener(e -> xoaDong());
        btnLamMoiGio.addActionListener(e -> lamMoiGio());
        btnThanhToan.addActionListener(e -> thanhToan());
        btnDonHang.addActionListener(e -> moDonHang());
        btnKhachHang.addActionListener(e -> moTraCuuKhachHang());

        setSize(1000, 680);
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
        dsSanPham = new ArrayList<>(duLieu.sanPham());
        dsTopping = new ArrayList<>(duLieu.topping());

        modelSanPham.setRowCount(0);
        for (SanPhamDto sp : dsSanPham) {
            String giaTheoSize = sp.kichThuoc().stream()
                    .map(k -> k.tenKichThuoc() + ": " + TienTe.dinhDang(k.giaBan()))
                    .collect(Collectors.joining("   |   "));
            modelSanPham.addRow(new Object[] {
                    sp.maSanPham(), sp.tenSanPham(), sp.tenDanhMuc(), giaTheoSize });
        }
    }

    private void themVaoGio() {
        int dong = tblSanPham.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm trong thực đơn",
                    "Chưa chọn sản phẩm", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SanPhamDto sanPham = dsSanPham.get(tblSanPham.convertRowIndexToModel(dong));

        ChonMonDialog hopThoai = new ChonMonDialog(this, sanPham, dsTopping);
        hopThoai.setVisible(true); // hộp thoại modal: chương trình chờ ở đây đến khi đóng

        MonTrongGio mon = hopThoai.getKetQua();
        if (mon != null) {
            gioHang.them(mon);
            capNhatGioHang();
        }
    }

    private void xoaDong() {
        int dong = tblGio.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng trong giỏ hàng",
                    "Chưa chọn dòng", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        gioHang.xoa(dong);
        capNhatGioHang();
    }

    private void lamMoiGio() {
        if (gioHang.laRong()) {
            return;
        }
        int chon = JOptionPane.showConfirmDialog(this, "Xóa toàn bộ giỏ hàng?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (chon == JOptionPane.YES_OPTION) {
            gioHang.lamMoi();
            capNhatGioHang();
        }
    }

    private void thanhToan() {
        if (gioHang.laRong()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống, hãy chọn món trước",
                    "Chưa có món", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ThanhToanDialog hopThoai = new ThanhToanDialog(this, apiClient, nguoiDung,
                gioHang.getCacMon(), gioHang.tongTienHang());
        hopThoai.setVisible(true); // hộp thoại modal: chờ ở đây đến khi đóng

        HoaDonDto hoaDon = hopThoai.getKetQua();
        if (hoaDon != null) { // null nghĩa là người dùng hủy hoặc chưa thanh toán xong
            gioHang.lamMoi();
            capNhatGioHang();
            hienTrangThai("Đã thanh toán đơn " + hoaDon.maDonHang(), false);
            new HoaDonDialog(this, hoaDon).setVisible(true);
        }
    }

    private void moDonHang() {
        if (cuaSoDonHang == null || !cuaSoDonHang.isDisplayable()) {
            cuaSoDonHang = new DonHangFrame(apiClient, nguoiDung);
            cuaSoDonHang.setVisible(true);
        } else {
            cuaSoDonHang.toFront(); // đã mở rồi thì đưa lên trước, không mở thêm
        }
    }

    private void moTraCuuKhachHang() {
        if (cuaSoKhachHang == null || !cuaSoKhachHang.isDisplayable()) {
            cuaSoKhachHang = new TraCuuKhachHangFrame(apiClient);
            cuaSoKhachHang.setVisible(true);
        } else {
            cuaSoKhachHang.toFront(); // đã mở rồi thì đưa lên trước, không mở thêm
        }
    }

    private void capNhatGioHang() {
        modelGio.setRowCount(0);
        for (MonTrongGio mon : gioHang.getCacMon()) {
            modelGio.addRow(new Object[] {
                    mon.getTenSanPham(), mon.getTenKichThuoc(), mon.getSoLuong(),
                    mon.moTaTopping(), mon.moTaTuyChon(), TienTe.dinhDang(mon.thanhTien()) });
        }
        lblTong.setText("Tổng tiền hàng: " + TienTe.dinhDang(gioHang.tongTienHang()) + " đ");
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