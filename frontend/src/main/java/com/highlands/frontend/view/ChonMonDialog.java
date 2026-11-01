package com.highlands.frontend.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.highlands.frontend.dto.KichThuocGiaDto;
import com.highlands.frontend.dto.SanPhamDto;
import com.highlands.frontend.dto.ToppingDto;
import com.highlands.frontend.model.MonTrongGio;
import com.highlands.frontend.util.TienTe;

/** Hộp thoại chọn size, số lượng, đường, đá, topping cho một sản phẩm. */
public class ChonMonDialog extends JDialog {

    private static final String[] MUC_DUONG = { "100%", "70%", "50%", "30%", "0%" };
    private static final String[] MUC_DA = { "Bình thường", "Ít đá", "Nhiều đá", "Không đá" };

    private final SanPhamDto sanPham;
    private final List<ToppingDto> dsTopping;
    private final boolean laDoUong; // Bánh không có đường, đá, topping

    private final JComboBox<KichThuocGiaDto> cboKichThuoc = new JComboBox<>();
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JComboBox<String> cboMucDuong = new JComboBox<>(MUC_DUONG);
    private final JComboBox<String> cboMucDa = new JComboBox<>(MUC_DA);
    private final JTextField txtGhiChu = new JTextField(22);
    private final List<JCheckBox> cacOTopping = new ArrayList<>();
    private final JLabel lblThanhTien = new JLabel(" ");
    private final JButton btnThem = new JButton("Thêm vào giỏ");
    private final JButton btnHuy = new JButton("Hủy");

    private MonTrongGio ketQua; // null nếu người dùng bấm Hủy

    public ChonMonDialog(JFrame cha, SanPhamDto sanPham, List<ToppingDto> dsTopping) {
        super(cha, "Chọn món: " + sanPham.tenSanPham(), true);
        this.sanPham = sanPham;
        this.dsTopping = dsTopping;
        this.laDoUong = !"Bánh".equals(sanPham.tenDanhMuc());
        khoiTaoGiaoDien();
    }

    public MonTrongGio getKetQua() {
        return ketQua;
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(8, 8));

        for (KichThuocGiaDto k : sanPham.kichThuoc()) {
            cboKichThuoc.addItem(k);
        }
        cboKichThuoc.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KichThuocGiaDto k) {
                    setText(k.tenKichThuoc() + " - " + TienTe.dinhDang(k.giaBan()));
                }
                return this;
            }
        });

        JPanel pnlThongTin = new JPanel(new GridBagLayout());
        pnlThongTin.setBorder(BorderFactory.createEmptyBorder(12, 14, 4, 14));
        themDong(pnlThongTin, 0, "Kích thước:", cboKichThuoc);
        themDong(pnlThongTin, 1, "Số lượng:", spnSoLuong);
        themDong(pnlThongTin, 2, "Mức đường:", cboMucDuong);
        themDong(pnlThongTin, 3, "Mức đá:", cboMucDa);
        themDong(pnlThongTin, 4, "Ghi chú:", txtGhiChu);
        add(pnlThongTin, BorderLayout.NORTH);

        JPanel pnlTopping = new JPanel(new GridLayout(0, 1));
        pnlTopping.setBorder(BorderFactory.createTitledBorder("Topping (giá mỗi ly)"));
        for (ToppingDto t : dsTopping) {
            JCheckBox o = new JCheckBox(t.tenTopping() + "  (+" + TienTe.dinhDang(t.giaBan()) + ")");
            o.setEnabled(laDoUong);
            o.addActionListener(e -> capNhatThanhTien());
            cacOTopping.add(o);
            pnlTopping.add(o);
        }
        add(pnlTopping, BorderLayout.CENTER);

        cboMucDuong.setEnabled(laDoUong);
        cboMucDa.setEnabled(laDoUong);
        cboKichThuoc.addActionListener(e -> capNhatThanhTien());
        spnSoLuong.addChangeListener(e -> capNhatThanhTien());

        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.setBorder(BorderFactory.createEmptyBorder(4, 14, 12, 14));
        lblThanhTien.setFont(new Font("SansSerif", Font.BOLD, 14));
        pnlDuoi.add(lblThanhTien, BorderLayout.WEST);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.add(btnHuy);
        pnlNut.add(btnThem);
        pnlDuoi.add(pnlNut, BorderLayout.EAST);
        add(pnlDuoi, BorderLayout.SOUTH);

        btnThem.addActionListener(e -> xacNhan());
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnThem);

        capNhatThanhTien();
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void themDong(JPanel p, int dong, String nhan, Component thanhPhan) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = dong;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        p.add(new JLabel(nhan), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(thanhPhan, c);
    }

    /** Tạo một dòng giỏ hàng từ lựa chọn hiện tại (null nếu chưa chọn size). */
    private MonTrongGio taoMon() {
        KichThuocGiaDto size = (KichThuocGiaDto) cboKichThuoc.getSelectedItem();
        if (size == null) {
            return null;
        }
        List<ToppingDto> daChon = new ArrayList<>();
        for (int i = 0; i < cacOTopping.size(); i++) {
            if (cacOTopping.get(i).isSelected()) {
                daChon.add(dsTopping.get(i));
            }
        }
        String ghiChu = txtGhiChu.getText().trim();
        if (ghiChu.length() > 255) {
            ghiChu = ghiChu.substring(0, 255); // cột ghi_chu trong database tối đa 255 ký tự
        }
        return new MonTrongGio(
                sanPham.id(), size.kichThuocId(), sanPham.tenSanPham(), size.tenKichThuoc(),
                size.giaBan(), daChon,
                laDoUong ? (String) cboMucDuong.getSelectedItem() : null,
                laDoUong ? (String) cboMucDa.getSelectedItem() : null,
                ghiChu.isEmpty() ? null : ghiChu,
                (Integer) spnSoLuong.getValue());
    }

    private void capNhatThanhTien() {
        MonTrongGio mon = taoMon();
        lblThanhTien.setText(mon == null ? " " : "Thành tiền: " + TienTe.dinhDang(mon.thanhTien()) + " đ");
    }

    private void xacNhan() {
        ketQua = taoMon();
        if (ketQua != null) {
            dispose();
        }
    }
}