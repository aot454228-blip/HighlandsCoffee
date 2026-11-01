package com.highlands.frontend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Giỏ hàng của một đơn tại quầy. */
public class GioHang {

    private final List<MonTrongGio> cacMon = new ArrayList<>();

    public void them(MonTrongGio mon) {
        cacMon.add(mon);
    }

    public void xoa(int viTri) {
        cacMon.remove(viTri);
    }

    public void lamMoi() {
        cacMon.clear();
    }

    public boolean laRong() {
        return cacMon.isEmpty();
    }

    /** Bên ngoài chỉ được đọc, không sửa trực tiếp danh sách. */
    public List<MonTrongGio> getCacMon() {
        return Collections.unmodifiableList(cacMon);
    }

    public BigDecimal tongTienHang() {
        BigDecimal tong = BigDecimal.ZERO;
        for (MonTrongGio mon : cacMon) {
            tong = tong.add(mon.thanhTien());
        }
        return tong;
    }
}