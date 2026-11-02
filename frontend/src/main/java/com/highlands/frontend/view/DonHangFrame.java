package com.highlands.frontend.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.highlands.frontend.api.ApiClient;
import com.highlands.frontend.api.ApiException;
import com.highlands.frontend.dto.DonHangDto;
import com.highlands.frontend.dto.DonHangTomTatDto;
import com.highlands.frontend.dto.LoginResponse;
import com.highlands.frontend.util.TienTe;
import com.highlands.frontend.util.TrangThaiDon;

/** Màn hình xử lý đơn hàng: xem danh sách, lọc, sắp xếp, đổi trạng thái, hủy đơn. */
public class DonHangFrame extends JFrame {

    private static final Color MAU_CHU_DAO = new Color(0xA6, 0x19, 0x2E);
    private static final String NHAN_TAT_CA = "Tất cả";

    private final ApiClient apiClient;
    private final LoginResponse nguoiDung;

    private final JComboBox<String> cboLoc = new JComboBox<>(taoNhanLoc());
    private final JComboBox<String> cboSapXep = new JComboBox<>(new String[] {
            "Mới nhất trước", "Cũ nhất trước", "Tổng tiền giảm dần", "Tổng tiền tăng dần" });

    // Dòng thứ i của bảng tương ứng phần tử thứ i của dsDon
    private List<DonHangTomTatDto> dsDon = new ArrayList<>();

    private final DefaultTableModel modelDon = new DefaultTableModel(
            new String[] { "Mã đơn", "Thời gian đặt", "Hình thức", "Tổng thanh toán", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblDon = new JTable(modelDon);

    private final JButton btnTaiLai = new JButton("Tải lại");
    private final JButton btnBuocTiep = new JButton("Chuyển bước tiếp theo");
    private final JButton btnHuyDon = new JButton("Hủy đơn");
    private final JButton btnDong = new JButton("Đóng");
    private final JLabel lblTrangThai = new JLabel(" ");

    public DonHangFrame(ApiClient apiClient, LoginResponse nguoiDung) {
        super("Highlands Coffee - Xử lý đơn hàng");
        this.apiClient = apiClient;
        this.nguoiDung = nguoiDung;
        khoiTaoGiaoDien();
        taiDanhSach();
    }

    private static String[] taoNhanLoc() {
        TrangThaiDon[] cacTrangThai = TrangThaiDon.values();
        String[] nhan = new String[cacTrangThai.length + 1];
        nhan[0] = NHAN_TAT_CA;
        for (int i = 0; i < cacTrangThai.length; i++) {
            nhan[i + 1] = cacTrangThai[i].getNhan();
        }
        return nhan;
    }

    private void khoiTaoGiaoDien() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // đóng cửa sổ này không tắt cả chương trình
        setLayout(new BorderLayout(8, 8));

        // --- Thanh trên: tiêu đề, bộ lọc, sắp xếp ---
        JLabel lblTieuDe = new JLabel("XỬ LÝ ĐƠN HÀNG");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(MAU_CHU_DAO);

        JPanel pnlBoLoc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBoLoc.add(new JLabel("Trạng thái:"));
        pnlBoLoc.add(cboLoc);
        pnlBoLoc.add(new JLabel("Sắp xếp:"));
        pnlBoLoc.add(cboSapXep);
        pnlBoLoc.add(btnTaiLai);

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        pnlTren.add(lblTieuDe, BorderLayout.WEST);
        pnlTren.add(pnlBoLoc, BorderLayout.EAST);
        add(pnlTren, BorderLayout.NORTH);

        // --- Bảng đơn hàng ---
        tblDon.setRowHeight(26);
        tblDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDon.getColumnModel().getColumn(0).setPreferredWidth(150);
        tblDon.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblDon.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblDon.getColumnModel().getColumn(3).setPreferredWidth(130);
        tblDon.getColumnModel().getColumn(4).setPreferredWidth(120);
        JScrollPane cuon = new JScrollPane(tblDon);
        cuon.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        add(cuon, BorderLayout.CENTER);

        // --- Thanh dưới: nút thao tác ---
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNut.add(btnBuocTiep);
        pnlNut.add(btnHuyDon);
        JPanel pnlDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlDong.add(btnDong);

        JPanel pnlDuoi = new JPanel(new BorderLayout(0, 6));
        pnlDuoi.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 14));
        pnlDuoi.add(pnlNut, BorderLayout.WEST);
        pnlDuoi.add(pnlDong, BorderLayout.EAST);
        pnlDuoi.add(lblTrangThai, BorderLayout.SOUTH);
        add(pnlDuoi, BorderLayout.SOUTH);

        cboLoc.addActionListener(e -> taiDanhSach());
        cboSapXep.addActionListener(e -> hienThiBang());
        btnTaiLai.addActionListener(e -> taiDanhSach());
        btnBuocTiep.addActionListener(e -> chuyenBuocTiep());
        btnHuyDon.addActionListener(e -> huyDon());
        btnDong.addActionListener(e -> dispose());
        tblDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatNut();
            }
        });

        capNhatNut();
        setSize(900, 560);
        setLocationRelativeTo(null);
    }

    private void taiDanhSach() {
        int viTri = cboLoc.getSelectedIndex();
        final String trangThai = viTri <= 0 ? null : TrangThaiDon.values()[viTri - 1].name();

        btnTaiLai.setEnabled(false);
        hienTrangThai("Đang tải danh sách đơn...", false);

        // Gọi API ở luồng nền để giao diện không bị đơ
        new SwingWorker<List<DonHangTomTatDto>, Void>() {
            @Override
            protected List<DonHangTomTatDto> doInBackground() {
                return apiClient.layDanhSachDon(trangThai);
            }

            @Override
            protected void done() {
                btnTaiLai.setEnabled(true);
                try {
                    dsDon = new ArrayList<>(get());
                    hienThiBang();
                    hienTrangThai("Có " + dsDon.size() + " đơn", false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    hienTrangThai(layThongBao(e), true);
                }
            }
        }.execute();
    }

    /** Sắp xếp danh sách theo lựa chọn rồi đổ vào bảng. */
    private void hienThiBang() {
        dsDon.sort(boSoSanh());
        modelDon.setRowCount(0);
        for (DonHangTomTatDto d : dsDon) {
            modelDon.addRow(new Object[] {
                    d.maDonHang(),
                    dinhDangThoiGian(d.thoiGianDat()),
                    nhanHinhThuc(d.hinhThucPhucVu()),
                    TienTe.dinhDang(d.tongThanhToan()) + " đ",
                    nhanTrangThai(d.trangThai()) });
        }
        capNhatNut();
    }

    /** Comparator theo lựa chọn ở ô "Sắp xếp". Chuỗi thời gian dạng ISO nên so sánh chữ là đúng thứ tự. */
    private Comparator<DonHangTomTatDto> boSoSanh() {
        Comparator<DonHangTomTatDto> theoThoiGian = Comparator.comparing(
                DonHangTomTatDto::thoiGianDat, Comparator.nullsLast(Comparator.<String>naturalOrder()));
        Comparator<DonHangTomTatDto> theoTien = Comparator.comparing(DonHangTomTatDto::tongThanhToan);

        return switch (cboSapXep.getSelectedIndex()) {
            case 1 -> theoThoiGian;
            case 2 -> theoTien.reversed();
            case 3 -> theoTien;
            default -> theoThoiGian.reversed();
        };
    }

    private DonHangTomTatDto donDangChon() {
        int dong = tblDon.getSelectedRow();
        return dong < 0 ? null : dsDon.get(dong);
    }

    /** Bật/tắt nút theo trạng thái của đơn đang chọn. */
    private void capNhatNut() {
        DonHangTomTatDto don = donDangChon();
        TrangThaiDon hienTai = don == null ? null : TrangThaiDon.tuMa(don.trangThai());
        TrangThaiDon tiep = hienTai == null ? null : hienTai.buocKeTiep();

        btnBuocTiep.setEnabled(tiep != null);
        btnBuocTiep.setText(tiep == null ? "Chuyển bước tiếp theo" : "Chuyển sang: " + tiep.getNhan());
        btnHuyDon.setEnabled(hienTai != null && !hienTai.laKetThuc());
    }

    private void chuyenBuocTiep() {
        DonHangTomTatDto don = donDangChon();
        TrangThaiDon hienTai = don == null ? null : TrangThaiDon.tuMa(don.trangThai());
        TrangThaiDon tiep = hienTai == null ? null : hienTai.buocKeTiep();
        if (tiep == null) {
            return;
        }
        int chon = JOptionPane.showConfirmDialog(this,
                "Chuyển đơn " + don.maDonHang() + " sang trạng thái \"" + tiep.getNhan() + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (chon == JOptionPane.YES_OPTION) {
            guiDoiTrangThai(don, tiep.name(), null);
        }
    }

    private void huyDon() {
        DonHangTomTatDto don = donDangChon();
        if (don == null) {
            return;
        }
        String lyDo = JOptionPane.showInputDialog(this,
                "Nhập lý do hủy đơn " + don.maDonHang() + ":", "Hủy đơn", JOptionPane.QUESTION_MESSAGE);
        if (lyDo == null) {
            return; // người dùng bấm Cancel
        }
        if (lyDo.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hủy đơn phải ghi lý do",
                    "Thiếu lý do", JOptionPane.WARNING_MESSAGE);
            return;
        }
        guiDoiTrangThai(don, TrangThaiDon.DA_HUY.name(), lyDo.trim());
    }

    private void guiDoiTrangThai(DonHangTomTatDto don, String trangThaiMoi, String lyDo) {
        setDangXuLy(true);
        hienTrangThai("Đang cập nhật đơn " + don.maDonHang() + "...", false);

        new SwingWorker<DonHangDto, Void>() {
            @Override
            protected DonHangDto doInBackground() {
                return apiClient.doiTrangThai(don.id(), nguoiDung.id(), trangThaiMoi, lyDo);
            }

            @Override
            protected void done() {
                setDangXuLy(false);
                try {
                    DonHangDto ketQua = get();
                    hienTrangThai("Đơn " + ketQua.maDonHang() + " đã chuyển sang: "
                            + nhanTrangThai(ketQua.trangThai()), false);
                    taiDanhSach(); // tải lại để bảng cập nhật
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    String noiDung = layThongBao(e);
                    hienTrangThai(noiDung, true);
                    JOptionPane.showMessageDialog(DonHangFrame.this, noiDung,
                            "Không thực hiện được", JOptionPane.WARNING_MESSAGE);
                }
            }
        }.execute();
    }

    private void setDangXuLy(boolean dangXuLy) {
        btnTaiLai.setEnabled(!dangXuLy);
        if (dangXuLy) {
            btnBuocTiep.setEnabled(false);
            btnHuyDon.setEnabled(false);
        } else {
            capNhatNut();
        }
    }

    private String layThongBao(ExecutionException e) {
        Throwable nguyenNhan = e.getCause();
        return (nguyenNhan instanceof ApiException)
                ? nguyenNhan.getMessage()
                : "Đã xảy ra lỗi không mong muốn";
    }

    private String nhanTrangThai(String ma) {
        TrangThaiDon t = TrangThaiDon.tuMa(ma);
        return t == null ? String.valueOf(ma) : t.getNhan();
    }

    private String nhanHinhThuc(String ma) {
        if ("TAI_CHO".equals(ma)) {
            return "Tại chỗ";
        }
        if ("MANG_DI".equals(ma)) {
            return "Mang đi";
        }
        return ma == null ? "" : ma;
    }

    /** Đổi "2026-11-02T01:56:20.123" thành "2026-11-02 01:56". */
    private String dinhDangThoiGian(String chu) {
        if (chu == null) {
            return "";
        }
        String s = chu.replace('T', ' ');
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    private void hienTrangThai(String noiDung, boolean laLoi) {
        lblTrangThai.setForeground(laLoi ? Color.RED : new Color(0x1B, 0x7F, 0x3B));
        lblTrangThai.setText(noiDung);
    }
}
