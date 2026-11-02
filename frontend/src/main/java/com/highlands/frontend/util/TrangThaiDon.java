package com.highlands.frontend.util;

/** Các trạng thái của đơn hàng, khớp với cột trang_thai trong database. */
public enum TrangThaiDon {
    MOI_TAO("Mới tạo"),
    DA_XAC_NHAN("Đã xác nhận"),
    DANG_CHUAN_BI("Đang chuẩn bị"),
    SAN_SANG("Sẵn sàng"),
    HOAN_THANH("Hoàn thành"),
    DA_HUY("Đã hủy");

    private final String nhan;

    TrangThaiDon(String nhan) {
        this.nhan = nhan;
    }

    /** Tên tiếng Việt để hiển thị trên giao diện. */
    public String getNhan() {
        return nhan;
    }

    public boolean laKetThuc() {
        return this == HOAN_THANH || this == DA_HUY;
    }

    /** Trạng thái kế tiếp trong quy trình; null nếu đơn đã kết thúc. */
    public TrangThaiDon buocKeTiep() {
        return switch (this) {
            case MOI_TAO -> DA_XAC_NHAN;
            case DA_XAC_NHAN -> DANG_CHUAN_BI;
            case DANG_CHUAN_BI -> SAN_SANG;
            case SAN_SANG -> HOAN_THANH;
            default -> null;
        };
    }

    /** Đổi mã chữ (ví dụ "MOI_TAO") thành trạng thái; null nếu mã lạ. */
    public static TrangThaiDon tuMa(String ma) {
        if (ma == null) {
            return null;
        }
        try {
            return valueOf(ma);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
