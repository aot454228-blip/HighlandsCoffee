package com.highlands.frontend.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Định dạng số tiền kiểu Việt Nam, ví dụ 29.000. */
public final class TienTe {

    private static final NumberFormat DINH_DANG = NumberFormat.getInstance(Locale.of("vi", "VN"));

    private TienTe() {
    }

    public static String dinhDang(BigDecimal soTien) {
        return DINH_DANG.format(soTien);
    }
}