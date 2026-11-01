package com.highlands.frontend.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.highlands.frontend.dto.DongHoaDonDto;
import com.highlands.frontend.dto.HoaDonDto;
import com.highlands.frontend.util.TienTe;

/** Hộp thoại hiển thị hóa đơn sau khi thanh toán. */
public class HoaDonDialog extends JDialog {

    private static final int RONG = 44; // số ký tự mỗi dòng của hóa đơn

    public HoaDonDialog(Frame chu, HoaDonDto hoaDon) {
        super(chu, "Hóa đơn " + hoaDon.maHoaDon(), true);

        JTextArea vung = new JTextArea(taoNoiDung(hoaDon));
        vung.setEditable(false);
        vung.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        vung.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        vung.setCaretPosition(0);

        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dispose());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlNut.add(btnDong);

        setLayout(new BorderLayout());
        add(new JScrollPane(vung), BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnDong);

        setSize(520, 520);
        setLocationRelativeTo(chu);
    }

    private static String taoNoiDung(HoaDonDto h) {
        StringBuilder sb = new StringBuilder();
        String gach = "-".repeat(RONG);

        sb.append(canGiua("HIGHLANDS COFFEE")).append('\n');
        sb.append(canGiua("HÓA ĐƠN THANH TOÁN")).append('\n');
        sb.append(gach).append('\n');
        sb.append("Mã hóa đơn : ").append(h.maHoaDon()).append('\n');
        sb.append("Mã đơn     : ").append(h.maDonHang()).append('\n');
        sb.append("Thời gian  : ").append(dinhDangThoiGian(h.thoiGianLap())).append('\n');
        sb.append(gach).append('\n');

        if (h.cacDong() != null) {
            for (DongHoaDonDto d : h.cacDong()) {
                String tenMon = d.soLuong() + " x " + d.tenSanPham() + " (" + d.tenKichThuoc() + ")";
                sb.append(haiCot(tenMon, TienTe.dinhDang(d.thanhTien()))).append('\n');
                sb.append("    Đơn giá: ").append(TienTe.dinhDang(d.donGia())).append('\n');
                if (d.cacTopping() != null && !d.cacTopping().isEmpty()) {
                    sb.append("    Topping: ").append(String.join(", ", d.cacTopping())).append('\n');
                }
            }
        }

        sb.append(gach).append('\n');
        sb.append(haiCot("Tổng tiền hàng:", tien(h.tongTienHang()))).append('\n');
        sb.append(haiCot("PHẢI TRẢ:", tien(h.soTienPhaiTra()))).append('\n');
        sb.append(haiCot("Phương thức:", "TIEN_MAT".equals(h.phuongThuc()) ? "Tiền mặt" : "Chuyển khoản")).append('\n');
        if (h.soTienKhachDua() != null) {
            sb.append(haiCot("Khách đưa:", tien(h.soTienKhachDua()))).append('\n');
        }
        if (h.tienThua() != null) {
            sb.append(haiCot("Tiền thừa:", tien(h.tienThua()))).append('\n');
        }
        sb.append(gach).append('\n');
        sb.append(canGiua("Cảm ơn quý khách!")).append('\n');
        return sb.toString();
    }

    private static String tien(BigDecimal soTien) {
        return soTien == null ? "-" : TienTe.dinhDang(soTien) + " đ";
    }

    private static String canGiua(String chu) {
        int le = Math.max(0, (RONG - chu.length()) / 2);
        return " ".repeat(le) + chu;
    }

    /** Chữ bên trái, số bên phải, cùng một dòng. */
    private static String haiCot(String trai, String phai) {
        int khoang = Math.max(1, RONG - trai.length() - phai.length());
        return trai + " ".repeat(khoang) + phai;
    }

    /** Đổi "2026-11-02T01:56:20.123" thành "2026-11-02 01:56:20". */
    private static String dinhDangThoiGian(String chu) {
        if (chu == null) {
            return "";
        }
        String s = chu.replace('T', ' ');
        return s.length() > 19 ? s.substring(0, 19) : s;
    }
}
