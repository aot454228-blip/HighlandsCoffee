/* =====================================================================
   HIGHLANDS COFFEE – LÂM VIÊN COMPLEX
   File SQL duy nhất cho MySQL 8.0 (yêu cầu >= 8.0.16 để CHECK có hiệu lực)

   CẤU TRÚC FILE
     PHẦN 1  Tạo database + 23 bảng + chỉ mục + ràng buộc   -> CHẠY LẠI AN TOÀN
     PHẦN 2  Tạo VIEW                                        -> CHẠY LẠI AN TOÀN
     PHẦN 3  Dữ liệu mẫu                                     -> CHỈ NÊN CHẠY MỘT LẦN
     PHẦN 4  Câu lệnh kiểm tra sau khi chạy                  -> chạy bao nhiêu lần cũng được
     PHẦN 5  (Tùy chọn) Thử ràng buộc – mỗi câu PHẢI báo lỗi

   Không có lệnh DROP nào: chạy lại không làm mất dữ liệu.
   Mật khẩu trong dữ liệu mẫu là chuỗi BCrypt (xem ghi chú ở PHẦN 3).

   PHIÊN BẢN THU HẸP PHẠM VI: đã BỎ phân hệ quản lý nhân viên
     (chuc_vu, nhan_vien, ca_lam_viec, lich_lam_viec, cham_cong).
   - tai_khoan giữ nguyên, vẫn 3 vai trò QUAN_LY / NHAN_VIEN / KHACH_HANG.
   - Người thực hiện thao tác (xử lý đơn, lập hóa đơn, thu tiền, lập/duyệt
     phiếu nhập, giao dịch kho) giờ được lưu bằng tai_khoan_id -> tai_khoan.
   - Lưu ý: file này dùng CREATE TABLE IF NOT EXISTS nên KHÔNG sửa được
     database cũ đã tạo từ phiên bản trước. Database cũ: dùng file
     highlands_coffee_migration.sql, hoặc tạo database mới.
   ===================================================================== */

/* ---------------------------------------------------------------------
   PHẦN 1 – CẤU TRÚC (chạy lại an toàn nhờ IF NOT EXISTS)
   --------------------------------------------------------------------- */
CREATE DATABASE IF NOT EXISTS highlands_coffee
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE highlands_coffee;
SET NAMES utf8mb4;

/* ---- 1. tai_khoan ------------------------------------------------- */
CREATE TABLE IF NOT EXISTS tai_khoan (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    ten_dang_nhap       VARCHAR(50)  NOT NULL,
    mat_khau_ma_hoa     VARCHAR(100) NOT NULL COMMENT 'Chuỗi BCrypt, không lưu mật khẩu thật',
    vai_tro             VARCHAR(20)  NOT NULL,
    trang_thai          VARCHAR(20)  NOT NULL DEFAULT 'HOAT_DONG',
    lan_dang_nhap_cuoi  DATETIME     NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tai_khoan_ten_dang_nhap (ten_dang_nhap),
    CONSTRAINT chk_tai_khoan_vai_tro   CHECK (vai_tro IN ('QUAN_LY','NHAN_VIEN','KHACH_HANG')),
    CONSTRAINT chk_tai_khoan_trang_thai CHECK (trang_thai IN ('HOAT_DONG','BI_KHOA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 2. khach_hang ------------------------------------------------ */
CREATE TABLE IF NOT EXISTS khach_hang (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    ma_khach_hang  VARCHAR(20)  NOT NULL,
    tai_khoan_id   BIGINT       NOT NULL,
    ho_ten         VARCHAR(100) NOT NULL,
    so_dien_thoai  VARCHAR(15)  NOT NULL,
    email          VARCHAR(100) NULL,
    ngay_sinh      DATE         NULL,
    gioi_tinh      VARCHAR(10)  NULL,
    dia_chi        VARCHAR(255) NULL,
    diem_tich_luy  INT          NOT NULL DEFAULT 0 COMMENT 'Số dư hiện tại, khớp tổng lich_su_diem.so_diem',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_khach_hang_ma (ma_khach_hang),
    UNIQUE KEY uq_khach_hang_tai_khoan (tai_khoan_id),
    UNIQUE KEY uq_khach_hang_sdt (so_dien_thoai),
    UNIQUE KEY uq_khach_hang_email (email),
    CONSTRAINT fk_khach_hang_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan (id),
    CONSTRAINT chk_khach_hang_gioi_tinh CHECK (gioi_tinh IN ('NAM','NU','KHAC')),
    CONSTRAINT chk_khach_hang_diem      CHECK (diem_tich_luy >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 3. danh_muc -------------------------------------------------- */
CREATE TABLE IF NOT EXISTS danh_muc (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    ten_danh_muc  VARCHAR(100) NOT NULL,
    mo_ta         VARCHAR(255) NULL,
    trang_thai    VARCHAR(20)  NOT NULL DEFAULT 'DANG_BAN',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_danh_muc_ten (ten_danh_muc),
    CONSTRAINT chk_danh_muc_trang_thai CHECK (trang_thai IN ('DANG_BAN','NGUNG_BAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 4. kich_thuoc ------------------------------------------------ */
CREATE TABLE IF NOT EXISTS kich_thuoc (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_kich_thuoc   VARCHAR(10)  NOT NULL,
    ten_kich_thuoc  VARCHAR(50)  NOT NULL,
    he_so_dinh_muc  DECIMAL(4,2) NOT NULL DEFAULT 1.00 COMMENT 'Nhân vào định mức công thức (size M = 1.00)',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_kich_thuoc_ma (ma_kich_thuoc),
    CONSTRAINT chk_kich_thuoc_he_so CHECK (he_so_dinh_muc > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 5. nhom_nguyen_lieu ------------------------------------------ */
CREATE TABLE IF NOT EXISTS nhom_nguyen_lieu (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    ten_nhom   VARCHAR(100) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_nhom_nguyen_lieu_ten (ten_nhom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 6. nguyen_lieu ----------------------------------------------- */
CREATE TABLE IF NOT EXISTS nguyen_lieu (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    ma_nguyen_lieu       VARCHAR(20)   NOT NULL,
    nhom_nguyen_lieu_id  BIGINT        NOT NULL,
    ten_nguyen_lieu      VARCHAR(150)  NOT NULL,
    don_vi_tinh          VARCHAR(20)   NOT NULL,
    so_luong_ton         DECIMAL(14,3) NOT NULL DEFAULT 0 COMMENT 'Tồn hiện tại, không được âm',
    muc_ton_toi_thieu    DECIMAL(14,3) NOT NULL DEFAULT 0,
    trang_thai           VARCHAR(20)   NOT NULL DEFAULT 'DANG_DUNG',
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_nguyen_lieu_ma (ma_nguyen_lieu),
    KEY idx_nguyen_lieu_nhom (nhom_nguyen_lieu_id),
    CONSTRAINT fk_nguyen_lieu_nhom FOREIGN KEY (nhom_nguyen_lieu_id) REFERENCES nhom_nguyen_lieu (id),
    CONSTRAINT chk_nguyen_lieu_ton       CHECK (so_luong_ton >= 0),
    CONSTRAINT chk_nguyen_lieu_toi_thieu CHECK (muc_ton_toi_thieu >= 0),
    CONSTRAINT chk_nguyen_lieu_trang_thai CHECK (trang_thai IN ('DANG_DUNG','NGUNG_DUNG'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 7. san_pham -------------------------------------------------- */
CREATE TABLE IF NOT EXISTS san_pham (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    ma_san_pham   VARCHAR(20)   NOT NULL,
    danh_muc_id   BIGINT        NOT NULL,
    ten_san_pham  VARCHAR(150)  NOT NULL,
    mo_ta         VARCHAR(1000) NULL,
    hinh_anh      VARCHAR(255)  NULL,
    trang_thai    VARCHAR(20)   NOT NULL DEFAULT 'DANG_BAN',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_san_pham_ma (ma_san_pham),
    KEY idx_san_pham_danh_muc (danh_muc_id),
    KEY idx_san_pham_ten (ten_san_pham),
    CONSTRAINT fk_san_pham_danh_muc FOREIGN KEY (danh_muc_id) REFERENCES danh_muc (id),
    CONSTRAINT chk_san_pham_trang_thai CHECK (trang_thai IN ('DANG_BAN','NGUNG_BAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 8. san_pham_kich_thuoc (giá theo size) ----------------------- */
CREATE TABLE IF NOT EXISTS san_pham_kich_thuoc (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    san_pham_id    BIGINT        NOT NULL,
    kich_thuoc_id  BIGINT        NOT NULL,
    gia_ban        DECIMAL(12,0) NOT NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sp_kich_thuoc (san_pham_id, kich_thuoc_id),
    KEY idx_sp_kich_thuoc_kt (kich_thuoc_id),
    CONSTRAINT fk_sp_kich_thuoc_sp FOREIGN KEY (san_pham_id)   REFERENCES san_pham (id),
    CONSTRAINT fk_sp_kich_thuoc_kt FOREIGN KEY (kich_thuoc_id) REFERENCES kich_thuoc (id),
    CONSTRAINT chk_sp_kich_thuoc_gia CHECK (gia_ban > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 9. topping -------------------------------------------------- */
CREATE TABLE IF NOT EXISTS topping (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    ten_topping     VARCHAR(100)  NOT NULL,
    gia_ban         DECIMAL(12,0) NOT NULL DEFAULT 0,
    nguyen_lieu_id  BIGINT        NULL COMMENT 'Nguyên liệu bị trừ kho khi chọn topping (nếu có)',
    luong_su_dung   DECIMAL(12,3) NULL COMMENT 'Lượng nguyên liệu cho 1 phần topping',
    hinh_anh        VARCHAR(255)  NULL,
    trang_thai      VARCHAR(20)   NOT NULL DEFAULT 'DANG_BAN',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_topping_ten (ten_topping),
    KEY idx_topping_nguyen_lieu (nguyen_lieu_id),
    CONSTRAINT fk_topping_nguyen_lieu FOREIGN KEY (nguyen_lieu_id) REFERENCES nguyen_lieu (id),
    CONSTRAINT chk_topping_gia        CHECK (gia_ban >= 0),
    CONSTRAINT chk_topping_luong      CHECK (luong_su_dung IS NULL OR luong_su_dung > 0),
    CONSTRAINT chk_topping_nl_luong   CHECK ((nguyen_lieu_id IS NULL) = (luong_su_dung IS NULL)),
    CONSTRAINT chk_topping_trang_thai CHECK (trang_thai IN ('DANG_BAN','NGUNG_BAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 10. cong_thuc (định mức cho 1 phần size M) -------------------- */
CREATE TABLE IF NOT EXISTS cong_thuc (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    san_pham_id     BIGINT        NOT NULL,
    nguyen_lieu_id  BIGINT        NOT NULL,
    so_luong        DECIMAL(12,3) NOT NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cong_thuc (san_pham_id, nguyen_lieu_id),
    KEY idx_cong_thuc_nguyen_lieu (nguyen_lieu_id),
    CONSTRAINT fk_cong_thuc_san_pham   FOREIGN KEY (san_pham_id)    REFERENCES san_pham (id),
    CONSTRAINT fk_cong_thuc_nguyen_lieu FOREIGN KEY (nguyen_lieu_id) REFERENCES nguyen_lieu (id),
    CONSTRAINT chk_cong_thuc_so_luong CHECK (so_luong > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 11. nha_cung_cap --------------------------------------------- */
CREATE TABLE IF NOT EXISTS nha_cung_cap (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    ma_nha_cung_cap   VARCHAR(20)  NOT NULL,
    ten_nha_cung_cap  VARCHAR(150) NOT NULL,
    nguoi_lien_he     VARCHAR(100) NULL,
    so_dien_thoai     VARCHAR(15)  NULL,
    email             VARCHAR(100) NULL,
    dia_chi           VARCHAR(255) NULL,
    trang_thai        VARCHAR(20)  NOT NULL DEFAULT 'HOP_TAC',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_nha_cung_cap_ma (ma_nha_cung_cap),
    CONSTRAINT chk_nha_cung_cap_trang_thai CHECK (trang_thai IN ('HOP_TAC','NGUNG_HOP_TAC'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 12. phieu_nhap ----------------------------------------------- */
CREATE TABLE IF NOT EXISTS phieu_nhap (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    ma_phieu_nhap    VARCHAR(30)   NOT NULL,
    nha_cung_cap_id  BIGINT        NOT NULL,
    nguoi_tao_id     BIGINT        NOT NULL COMMENT 'tai_khoan.id của người lập phiếu (QUAN_LY/NHAN_VIEN)',
    nguoi_duyet_id   BIGINT        NULL COMMENT 'tai_khoan.id của người duyệt (NULL = chưa duyệt)',
    ngay_nhap        DATE          NOT NULL,
    tong_tien        DECIMAL(14,0) NOT NULL DEFAULT 0,
    trang_thai       VARCHAR(20)   NOT NULL DEFAULT 'CHO_DUYET',
    ghi_chu          VARCHAR(255)  NULL,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_phieu_nhap_ma (ma_phieu_nhap),
    KEY idx_phieu_nhap_ncc (nha_cung_cap_id),
    KEY idx_phieu_nhap_nguoi_tao (nguoi_tao_id),
    KEY idx_phieu_nhap_nguoi_duyet (nguoi_duyet_id),
    CONSTRAINT fk_phieu_nhap_ncc        FOREIGN KEY (nha_cung_cap_id) REFERENCES nha_cung_cap (id),
    CONSTRAINT fk_phieu_nhap_nguoi_tao  FOREIGN KEY (nguoi_tao_id)    REFERENCES tai_khoan (id),
    CONSTRAINT fk_phieu_nhap_nguoi_duyet FOREIGN KEY (nguoi_duyet_id) REFERENCES tai_khoan (id),
    CONSTRAINT chk_phieu_nhap_tong_tien CHECK (tong_tien >= 0),
    CONSTRAINT chk_phieu_nhap_trang_thai CHECK (trang_thai IN ('CHO_DUYET','DA_NHAP_KHO','DA_HUY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 13. chi_tiet_phieu_nhap -------------------------------------- */
CREATE TABLE IF NOT EXISTS chi_tiet_phieu_nhap (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    phieu_nhap_id   BIGINT        NOT NULL,
    nguyen_lieu_id  BIGINT        NOT NULL,
    so_luong        DECIMAL(14,3) NOT NULL,
    don_gia         DECIMAL(12,0) NOT NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ct_phieu_nhap (phieu_nhap_id, nguyen_lieu_id),
    KEY idx_ct_phieu_nhap_nl (nguyen_lieu_id),
    CONSTRAINT fk_ct_phieu_nhap_phieu FOREIGN KEY (phieu_nhap_id)  REFERENCES phieu_nhap (id),
    CONSTRAINT fk_ct_phieu_nhap_nl    FOREIGN KEY (nguyen_lieu_id) REFERENCES nguyen_lieu (id),
    CONSTRAINT chk_ct_phieu_nhap_sl  CHECK (so_luong > 0),
    CONSTRAINT chk_ct_phieu_nhap_gia CHECK (don_gia >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 14. khuyen_mai ----------------------------------------------- */
CREATE TABLE IF NOT EXISTS khuyen_mai (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    ten_khuyen_mai   VARCHAR(150)  NOT NULL,
    ma_code          VARCHAR(30)   NULL COMMENT 'NULL = khuyến mãi tự áp dụng, không cần nhập mã',
    loai_giam        VARCHAR(20)   NOT NULL,
    gia_tri_giam     DECIMAL(12,2) NOT NULL,
    giam_toi_da      DECIMAL(12,0) NULL,
    don_toi_thieu    DECIMAL(12,0) NOT NULL DEFAULT 0,
    ngay_bat_dau     DATETIME      NOT NULL,
    ngay_ket_thuc    DATETIME      NOT NULL,
    so_luot_toi_da   INT           NULL COMMENT 'NULL = không giới hạn',
    so_luot_da_dung  INT           NOT NULL DEFAULT 0,
    hinh_anh         VARCHAR(255)  NULL,
    trang_thai       VARCHAR(20)   NOT NULL DEFAULT 'HOAT_DONG',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_khuyen_mai_code (ma_code),
    CONSTRAINT chk_khuyen_mai_loai     CHECK (loai_giam IN ('PHAN_TRAM','SO_TIEN')),
    CONSTRAINT chk_khuyen_mai_gia_tri  CHECK (gia_tri_giam > 0 AND (loai_giam = 'SO_TIEN' OR gia_tri_giam <= 100)),
    CONSTRAINT chk_khuyen_mai_toi_da   CHECK (giam_toi_da IS NULL OR giam_toi_da > 0),
    CONSTRAINT chk_khuyen_mai_don_min  CHECK (don_toi_thieu >= 0),
    CONSTRAINT chk_khuyen_mai_ngay     CHECK (ngay_ket_thuc >= ngay_bat_dau),
    CONSTRAINT chk_khuyen_mai_luot     CHECK (so_luot_da_dung >= 0 AND (so_luot_toi_da IS NULL OR so_luot_toi_da > 0)),
    CONSTRAINT chk_khuyen_mai_trang_thai CHECK (trang_thai IN ('HOAT_DONG','TAM_DUNG'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 15. don_hang ------------------------------------------------- */
CREATE TABLE IF NOT EXISTS don_hang (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    ma_don_hang           VARCHAR(30)   NOT NULL,
    khach_hang_id         BIGINT        NULL COMMENT 'NULL = khách vãng lai',
    tai_khoan_id          BIGINT        NULL COMMENT 'Tài khoản (QUAN_LY/NHAN_VIEN) tiếp nhận/xử lý đơn; NULL = chưa có người nhận',
    khuyen_mai_id         BIGINT        NULL,
    kenh_dat              VARCHAR(20)   NOT NULL,
    hinh_thuc_phuc_vu     VARCHAR(20)   NOT NULL,
    trang_thai            VARCHAR(20)   NOT NULL DEFAULT 'MOI_TAO',
    ten_nguoi_nhan        VARCHAR(100)  NULL,
    so_dien_thoai_nhan    VARCHAR(15)   NULL,
    dia_chi_giao_hang     VARCHAR(255)  NULL,
    ghi_chu               VARCHAR(255)  NULL,
    tong_tien_hang        DECIMAL(14,0) NOT NULL DEFAULT 0,
    tien_giam_khuyen_mai  DECIMAL(14,0) NOT NULL DEFAULT 0,
    diem_su_dung          INT           NOT NULL DEFAULT 0,
    tien_giam_diem        DECIMAL(14,0) NOT NULL DEFAULT 0,
    tong_thanh_toan       DECIMAL(14,0) NOT NULL DEFAULT 0,
    thoi_gian_dat         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoi_gian_hoan_thanh  DATETIME      NULL,
    thoi_gian_huy         DATETIME      NULL,
    ly_do_huy             VARCHAR(255)  NULL,
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_don_hang_ma (ma_don_hang),
    KEY idx_don_hang_khach_hang (khach_hang_id, thoi_gian_dat),
    KEY idx_don_hang_tai_khoan (tai_khoan_id),
    KEY idx_don_hang_khuyen_mai (khuyen_mai_id),
    KEY idx_don_hang_trang_thai (trang_thai),
    KEY idx_don_hang_thoi_gian_dat (thoi_gian_dat),
    CONSTRAINT fk_don_hang_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang (id),
    CONSTRAINT fk_don_hang_tai_khoan  FOREIGN KEY (tai_khoan_id)  REFERENCES tai_khoan (id),
    CONSTRAINT fk_don_hang_khuyen_mai FOREIGN KEY (khuyen_mai_id) REFERENCES khuyen_mai (id),
    CONSTRAINT chk_don_hang_kenh      CHECK (kenh_dat IN ('TAI_QUAY','ONLINE')),
    CONSTRAINT chk_don_hang_hinh_thuc CHECK (hinh_thuc_phuc_vu IN ('TAI_CHO','MANG_DI','GIAO_HANG')),
    CONSTRAINT chk_don_hang_trang_thai CHECK (trang_thai IN ('MOI_TAO','DA_XAC_NHAN','DANG_CHUAN_BI','SAN_SANG','HOAN_THANH','DA_HUY')),
    CONSTRAINT chk_don_hang_tien      CHECK (tong_tien_hang >= 0 AND tien_giam_khuyen_mai >= 0 AND tien_giam_diem >= 0 AND diem_su_dung >= 0),
    CONSTRAINT chk_don_hang_tong      CHECK (tong_thanh_toan >= 0 AND tong_thanh_toan = tong_tien_hang - tien_giam_khuyen_mai - tien_giam_diem),
    CONSTRAINT chk_don_hang_giao_hang CHECK (hinh_thuc_phuc_vu <> 'GIAO_HANG' OR dia_chi_giao_hang IS NOT NULL),
    CONSTRAINT chk_don_hang_huy       CHECK (trang_thai <> 'DA_HUY' OR thoi_gian_huy IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 16. chi_tiet_don_hang ---------------------------------------- */
CREATE TABLE IF NOT EXISTS chi_tiet_don_hang (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    don_hang_id    BIGINT        NOT NULL,
    san_pham_id    BIGINT        NOT NULL,
    kich_thuoc_id  BIGINT        NOT NULL,
    ten_san_pham   VARCHAR(150)  NOT NULL COMMENT 'Chụp tên tại lúc đặt',
    don_gia        DECIMAL(12,0) NOT NULL COMMENT 'Giá size tại lúc đặt',
    so_luong       INT           NOT NULL,
    muc_duong      VARCHAR(20)   NULL,
    muc_da         VARCHAR(20)   NULL,
    ghi_chu        VARCHAR(255)  NULL,
    thanh_tien     DECIMAL(14,0) NOT NULL COMMENT '(don_gia + tổng topping) x so_luong, chốt lúc đặt',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ct_don_hang_don (don_hang_id),
    KEY idx_ct_don_hang_sp (san_pham_id),
    KEY idx_ct_don_hang_kt (kich_thuoc_id),
    CONSTRAINT fk_ct_don_hang_don FOREIGN KEY (don_hang_id)   REFERENCES don_hang (id),
    CONSTRAINT fk_ct_don_hang_sp  FOREIGN KEY (san_pham_id)   REFERENCES san_pham (id),
    CONSTRAINT fk_ct_don_hang_kt  FOREIGN KEY (kich_thuoc_id) REFERENCES kich_thuoc (id),
    CONSTRAINT chk_ct_don_hang_gia CHECK (don_gia >= 0),
    CONSTRAINT chk_ct_don_hang_sl  CHECK (so_luong > 0),
    CONSTRAINT chk_ct_don_hang_tt  CHECK (thanh_tien >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 17. chi_tiet_don_hang_topping -------------------------------- */
CREATE TABLE IF NOT EXISTS chi_tiet_don_hang_topping (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    chi_tiet_don_hang_id  BIGINT        NOT NULL,
    topping_id            BIGINT        NOT NULL,
    so_luong              INT           NOT NULL DEFAULT 1 COMMENT 'Số phần topping cho mỗi ly',
    don_gia               DECIMAL(12,0) NOT NULL COMMENT 'Giá topping tại lúc đặt',
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ct_dh_topping (chi_tiet_don_hang_id, topping_id),
    KEY idx_ct_dh_topping_tp (topping_id),
    CONSTRAINT fk_ct_dh_topping_ct FOREIGN KEY (chi_tiet_don_hang_id) REFERENCES chi_tiet_don_hang (id),
    CONSTRAINT fk_ct_dh_topping_tp FOREIGN KEY (topping_id)           REFERENCES topping (id),
    CONSTRAINT chk_ct_dh_topping_sl  CHECK (so_luong > 0),
    CONSTRAINT chk_ct_dh_topping_gia CHECK (don_gia >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 18. hoa_don -------------------------------------------------- */
CREATE TABLE IF NOT EXISTS hoa_don (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    ma_hoa_don        VARCHAR(30)   NOT NULL,
    don_hang_id       BIGINT        NOT NULL,
    tai_khoan_id      BIGINT        NULL COMMENT 'Tài khoản lập hóa đơn',
    thoi_gian_lap     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    so_tien_phai_tra  DECIMAL(14,0) NOT NULL,
    trang_thai        VARCHAR(20)   NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_hoa_don_ma (ma_hoa_don),
    UNIQUE KEY uq_hoa_don_don_hang (don_hang_id),
    KEY idx_hoa_don_tai_khoan (tai_khoan_id),
    CONSTRAINT fk_hoa_don_don_hang  FOREIGN KEY (don_hang_id)  REFERENCES don_hang (id),
    CONSTRAINT fk_hoa_don_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan (id),
    CONSTRAINT chk_hoa_don_tien      CHECK (so_tien_phai_tra >= 0),
    CONSTRAINT chk_hoa_don_trang_thai CHECK (trang_thai IN ('CHUA_THANH_TOAN','DA_THANH_TOAN','DA_HOAN_TIEN','DA_HUY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 19. thanh_toan ----------------------------------------------- */
CREATE TABLE IF NOT EXISTS thanh_toan (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    hoa_don_id            BIGINT        NOT NULL,
    tai_khoan_id          BIGINT        NULL COMMENT 'Tài khoản thu tiền',
    phuong_thuc           VARCHAR(20)   NOT NULL,
    so_tien               DECIMAL(14,0) NOT NULL,
    so_tien_khach_dua     DECIMAL(14,0) NULL COMMENT 'Chỉ tiền mặt',
    tien_thua             DECIMAL(14,0) NULL COMMENT 'Chỉ tiền mặt',
    ma_giao_dich          VARCHAR(50)   NULL COMMENT 'Chỉ chuyển khoản',
    trang_thai            VARCHAR(20)   NOT NULL DEFAULT 'THANH_CONG',
    thoi_gian_thanh_toan  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_thanh_toan_ma_gd (ma_giao_dich),
    KEY idx_thanh_toan_hoa_don (hoa_don_id),
    KEY idx_thanh_toan_tai_khoan (tai_khoan_id),
    KEY idx_thanh_toan_thoi_gian (thoi_gian_thanh_toan),
    CONSTRAINT fk_thanh_toan_hoa_don  FOREIGN KEY (hoa_don_id)  REFERENCES hoa_don (id),
    CONSTRAINT fk_thanh_toan_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan (id),
    CONSTRAINT chk_thanh_toan_phuong_thuc CHECK (phuong_thuc IN ('TIEN_MAT','CHUYEN_KHOAN')),
    CONSTRAINT chk_thanh_toan_so_tien     CHECK (so_tien > 0),
    CONSTRAINT chk_thanh_toan_khach_dua   CHECK (so_tien_khach_dua IS NULL OR so_tien_khach_dua >= so_tien),
    CONSTRAINT chk_thanh_toan_trang_thai  CHECK (trang_thai IN ('THANH_CONG','THAT_BAI','DA_HOAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 20. lich_su_trang_thai_don_hang (chỉ created_at) -------------- */
CREATE TABLE IF NOT EXISTS lich_su_trang_thai_don_hang (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    don_hang_id     BIGINT       NOT NULL,
    trang_thai_cu   VARCHAR(20)  NULL,
    trang_thai_moi  VARCHAR(20)  NOT NULL,
    tai_khoan_id    BIGINT       NULL,
    ghi_chu         VARCHAR(255) NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ls_tt_don_hang (don_hang_id, created_at),
    KEY idx_ls_tt_tai_khoan (tai_khoan_id),
    CONSTRAINT fk_ls_tt_don_hang  FOREIGN KEY (don_hang_id)  REFERENCES don_hang (id),
    CONSTRAINT fk_ls_tt_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan (id),
    CONSTRAINT chk_ls_tt_moi CHECK (trang_thai_moi IN ('MOI_TAO','DA_XAC_NHAN','DANG_CHUAN_BI','SAN_SANG','HOAN_THANH','DA_HUY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 21. giao_dich_kho – SỔ CÁI KHO (chỉ created_at) --------------- */
CREATE TABLE IF NOT EXISTS giao_dich_kho (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    nguyen_lieu_id          BIGINT        NOT NULL,
    loai_giao_dich          VARCHAR(20)   NOT NULL,
    so_luong_thay_doi       DECIMAL(14,3) NOT NULL COMMENT 'Có dấu: nhập/hoàn dương, xuất âm',
    ton_sau_giao_dich       DECIMAL(14,3) NOT NULL,
    don_gia                 DECIMAL(12,0) NULL,
    chi_tiet_phieu_nhap_id  BIGINT        NULL,
    don_hang_id             BIGINT        NULL,
    giao_dich_goc_id        BIGINT        NULL COMMENT 'HOAN_KHO trỏ về dòng XUAT_BAN_HANG gốc',
    tai_khoan_id            BIGINT        NULL COMMENT 'Tài khoản thực hiện giao dịch kho',
    ly_do                   VARCHAR(255)  NULL,
    created_at              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_gd_kho_goc (giao_dich_goc_id),
    UNIQUE KEY uq_gd_kho_don_nl_loai (don_hang_id, nguyen_lieu_id, loai_giao_dich),
    KEY idx_gd_kho_nl_thoi_gian (nguyen_lieu_id, created_at),
    KEY idx_gd_kho_ct_phieu (chi_tiet_phieu_nhap_id),
    KEY idx_gd_kho_tai_khoan (tai_khoan_id),
    CONSTRAINT fk_gd_kho_nl       FOREIGN KEY (nguyen_lieu_id)         REFERENCES nguyen_lieu (id),
    CONSTRAINT fk_gd_kho_ct_phieu FOREIGN KEY (chi_tiet_phieu_nhap_id) REFERENCES chi_tiet_phieu_nhap (id),
    CONSTRAINT fk_gd_kho_don_hang FOREIGN KEY (don_hang_id)            REFERENCES don_hang (id),
    CONSTRAINT fk_gd_kho_goc      FOREIGN KEY (giao_dich_goc_id)       REFERENCES giao_dich_kho (id),
    CONSTRAINT fk_gd_kho_tai_khoan FOREIGN KEY (tai_khoan_id)          REFERENCES tai_khoan (id),
    CONSTRAINT chk_gd_kho_loai  CHECK (loai_giao_dich IN ('NHAP_KHO','XUAT_BAN_HANG','HOAN_KHO','XUAT_HAO_HUT','DIEU_CHINH')),
    CONSTRAINT chk_gd_kho_dau   CHECK (
           (loai_giao_dich IN ('NHAP_KHO','HOAN_KHO') AND so_luong_thay_doi > 0)
        OR (loai_giao_dich IN ('XUAT_BAN_HANG','XUAT_HAO_HUT') AND so_luong_thay_doi < 0)
        OR (loai_giao_dich = 'DIEU_CHINH' AND so_luong_thay_doi <> 0)),
    CONSTRAINT chk_gd_kho_ton   CHECK (ton_sau_giao_dich >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 22. lich_su_diem – SỔ CÁI ĐIỂM (chỉ created_at) --------------- */
CREATE TABLE IF NOT EXISTS lich_su_diem (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    khach_hang_id       BIGINT       NOT NULL,
    don_hang_id         BIGINT       NULL,
    loai_giao_dich      VARCHAR(20)  NOT NULL,
    so_diem             INT          NOT NULL COMMENT 'Có dấu: tích/hoàn dương, dùng âm',
    diem_sau_giao_dich  INT          NOT NULL,
    ghi_chu             VARCHAR(255) NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ls_diem_don_loai (don_hang_id, loai_giao_dich),
    KEY idx_ls_diem_khach_hang (khach_hang_id, created_at),
    CONSTRAINT fk_ls_diem_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang (id),
    CONSTRAINT fk_ls_diem_don_hang   FOREIGN KEY (don_hang_id)   REFERENCES don_hang (id),
    CONSTRAINT chk_ls_diem_loai CHECK (loai_giao_dich IN ('TICH_DIEM','SU_DUNG','HOAN_DIEM','DIEU_CHINH')),
    CONSTRAINT chk_ls_diem_so   CHECK (so_diem <> 0),
    CONSTRAINT chk_ls_diem_sau  CHECK (diem_sau_giao_dich >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---- 23. danh_gia_phan_hoi ---------------------------------------- */
CREATE TABLE IF NOT EXISTS danh_gia_phan_hoi (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    khach_hang_id  BIGINT        NOT NULL,
    san_pham_id    BIGINT        NULL COMMENT 'NULL = phản hồi chung về cửa hàng',
    so_sao         TINYINT       NULL,
    noi_dung       VARCHAR(1000) NULL,
    trang_thai     VARCHAR(20)   NOT NULL DEFAULT 'HIEN_THI',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_danh_gia_khach_hang (khach_hang_id),
    KEY idx_danh_gia_san_pham (san_pham_id),
    CONSTRAINT fk_danh_gia_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang (id),
    CONSTRAINT fk_danh_gia_san_pham   FOREIGN KEY (san_pham_id)   REFERENCES san_pham (id),
    CONSTRAINT chk_danh_gia_sao  CHECK (so_sao BETWEEN 1 AND 5),
    CONSTRAINT chk_danh_gia_sp_sao CHECK (san_pham_id IS NULL OR so_sao IS NOT NULL),
    CONSTRAINT chk_danh_gia_trang_thai CHECK (trang_thai IN ('HIEN_THI','AN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* ---------------------------------------------------------------------
   PHẦN 2 – VIEW (CREATE OR REPLACE => chạy lại an toàn)
   Doanh thu chỉ tính đơn HOAN_THANH. Đơn DA_HUY không tính.
   --------------------------------------------------------------------- */
CREATE OR REPLACE VIEW v_chi_tiet_don_hang AS
SELECT dh.id AS don_hang_id, dh.ma_don_hang, dh.trang_thai AS trang_thai_don,
       ct.id AS chi_tiet_id, ct.ten_san_pham, kt.ma_kich_thuoc,
       ct.don_gia, ct.so_luong,
       COALESCE((SELECT GROUP_CONCAT(CONCAT(tp.ten_topping, ' x', t.so_luong) SEPARATOR ', ')
                 FROM chi_tiet_don_hang_topping t JOIN topping tp ON tp.id = t.topping_id
                 WHERE t.chi_tiet_don_hang_id = ct.id), '') AS danh_sach_topping,
       ct.muc_duong, ct.muc_da, ct.thanh_tien
FROM don_hang dh
JOIN chi_tiet_don_hang ct ON ct.don_hang_id = dh.id
JOIN kich_thuoc kt        ON kt.id = ct.kich_thuoc_id;

CREATE OR REPLACE VIEW v_doanh_thu_theo_ngay AS
SELECT DATE(thoi_gian_dat) AS ngay,
       COUNT(*)            AS so_don,
       SUM(tong_tien_hang) AS tong_tien_hang,
       SUM(tien_giam_khuyen_mai + tien_giam_diem) AS tong_giam_gia,
       SUM(tong_thanh_toan) AS doanh_thu
FROM don_hang
WHERE trang_thai = 'HOAN_THANH'
GROUP BY DATE(thoi_gian_dat);

CREATE OR REPLACE VIEW v_san_pham_ban_chay AS
SELECT ct.san_pham_id, sp.ma_san_pham, sp.ten_san_pham,
       SUM(ct.so_luong)   AS so_luong_ban,
       SUM(ct.thanh_tien) AS doanh_thu
FROM chi_tiet_don_hang ct
JOIN don_hang dh ON dh.id = ct.don_hang_id AND dh.trang_thai = 'HOAN_THANH'
JOIN san_pham sp ON sp.id = ct.san_pham_id
GROUP BY ct.san_pham_id, sp.ma_san_pham, sp.ten_san_pham;

CREATE OR REPLACE VIEW v_nguyen_lieu_duoi_muc_toi_thieu AS
SELECT nl.id, nl.ma_nguyen_lieu, nl.ten_nguyen_lieu, nl.don_vi_tinh,
       nl.so_luong_ton, nl.muc_ton_toi_thieu,
       nl.muc_ton_toi_thieu - nl.so_luong_ton AS can_nhap_them
FROM nguyen_lieu nl
WHERE nl.trang_thai = 'DANG_DUNG' AND nl.so_luong_ton < nl.muc_ton_toi_thieu;

/* ---------------------------------------------------------------------
   PHẦN 3 – DỮ LIỆU MẪU  (CHỈ NÊN CHẠY MỘT LẦN)
   - Mọi dữ liệu là GIẢ, chỉ minh họa đồ án. Tên sản phẩm/giá KHÔNG phải
     menu hay giá chính thức của Highlands Coffee.
   - Dùng INSERT IGNORE + id cố định: nếu lỡ chạy lại sẽ không nhân đôi,
     nhưng vẫn nên chạy 1 lần. Không chạy lại sau khi đã nhập dữ liệu thật.
   - MẬT KHẨU: cột mat_khau_ma_hoa là chuỗi BCrypt ($2a$10$...), Spring
     Security (BCryptPasswordEncoder) đọc được trực tiếp. Mật khẩu gốc để
     đăng nhập thử (chỉ dùng khi test, đổi ngay khi nộp/triển khai):
         quanly01              -> Admin@123
         thungan01, phache01.. -> Nv@123456
         khach01..khach03      -> Kh@123456
     Muốn tạo hash mới trong Spring Boot:
         new BCryptPasswordEncoder().encode("MatKhauMoi")
   --------------------------------------------------------------------- */
USE highlands_coffee;
SET NAMES utf8mb4;

-- 3.1 Tài khoản (quản lý, nhân viên, khách hàng) và hồ sơ khách hàng -----
INSERT IGNORE INTO tai_khoan (id, ten_dang_nhap, mat_khau_ma_hoa, vai_tro) VALUES
 (1, 'quanly01',  '$2a$10$1FuoqILf6KAiXUp7ctupDudX5NHoqzZqAvD4Zj5Aj5rkImhF4KVvm', 'QUAN_LY'),
 (2, 'thungan01', '$2a$10$Gk.5RlX0Fpeaevsk/MeBjeUhexp3bKB3gnGRrR2R3xIGJZcf7MNqO', 'NHAN_VIEN'),
 (3, 'phache01',  '$2a$10$Gk.5RlX0Fpeaevsk/MeBjeUhexp3bKB3gnGRrR2R3xIGJZcf7MNqO', 'NHAN_VIEN'),
 (4, 'phache02',  '$2a$10$Gk.5RlX0Fpeaevsk/MeBjeUhexp3bKB3gnGRrR2R3xIGJZcf7MNqO', 'NHAN_VIEN'),
 (5, 'phucvu01',  '$2a$10$Gk.5RlX0Fpeaevsk/MeBjeUhexp3bKB3gnGRrR2R3xIGJZcf7MNqO', 'NHAN_VIEN'),
 (6, 'khach01',   '$2a$10$64K.AfkGCf0fyZ8qukKc3eRCDCPUZKfsjw/0Os7UEgfHchTIAW1uK', 'KHACH_HANG'),
 (7, 'khach02',   '$2a$10$64K.AfkGCf0fyZ8qukKc3eRCDCPUZKfsjw/0Os7UEgfHchTIAW1uK', 'KHACH_HANG'),
 (8, 'khach03',   '$2a$10$64K.AfkGCf0fyZ8qukKc3eRCDCPUZKfsjw/0Os7UEgfHchTIAW1uK', 'KHACH_HANG');

INSERT IGNORE INTO khach_hang (id, ma_khach_hang, tai_khoan_id, ho_ten, so_dien_thoai, email, ngay_sinh, gioi_tinh, dia_chi, diem_tich_luy) VALUES
 (1, 'KH001', 6, 'Đỗ Thị Lan',    '0910000001', 'lan.demo@example.com',  '2002-04-02', 'NU',  'Dịch Vọng Hậu, Cầu Giấy, Hà Nội', 40),
 (2, 'KH002', 7, 'Vũ Hoàng Nam',  '0910000002', 'nam.demo@example.com',  '1998-09-19', 'NAM', 'Mai Dịch, Cầu Giấy, Hà Nội', 8),
 (3, 'KH003', 8, 'Bùi Ngọc Mai',  '0910000003', NULL,                    '2001-12-25', 'NU',  'Quan Hoa, Cầu Giấy, Hà Nội', 0);

-- 3.2 Thực đơn --------------------------------------------------------
INSERT IGNORE INTO danh_muc (id, ten_danh_muc, mo_ta) VALUES
 (1, 'Cà phê', 'Các món cà phê'), (2, 'Trà', 'Các món trà'),
 (3, 'Freeze', 'Đá xay'),          (4, 'Bánh', 'Bánh và món ăn nhẹ');

INSERT IGNORE INTO kich_thuoc (id, ma_kich_thuoc, ten_kich_thuoc, he_so_dinh_muc) VALUES
 (1, 'S',   'Nhỏ',          0.80),
 (2, 'M',   'Vừa',          1.00),
 (3, 'L',   'Lớn',          1.30),
 (4, 'STD', 'Tiêu chuẩn',   1.00);

INSERT IGNORE INTO nhom_nguyen_lieu (id, ten_nhom) VALUES
 (1, 'Cà phê'), (2, 'Sữa và kem'), (3, 'Trà, siro, bột'), (4, 'Topping'), (5, 'Đá và khác'), (6, 'Bánh nhập sẵn');

-- nguyen_lieu: tồn hiện tại = tổng sổ kho (đã tính sẵn)
INSERT IGNORE INTO nguyen_lieu (id, ma_nguyen_lieu, nhom_nguyen_lieu_id, ten_nguyen_lieu, don_vi_tinh, so_luong_ton, muc_ton_toi_thieu) VALUES
  (1, 'NL001', 1, 'Cà phê Phin rang xay', 'g', 4908.4, 1000),
  (2, 'NL002', 2, 'Sữa đặc', 'g', 9868.5, 2000),
  (3, 'NL003', 5, 'Đá viên', 'g', 99019, 20000),
  (4, 'NL004', 2, 'Sữa tươi', 'ml', 19282, 3000),
  (5, 'NL005', 3, 'Trà đen', 'g', 1992, 500),
  (6, 'NL006', 3, 'Siro đường', 'ml', 4965, 1000),
  (7, 'NL007', 4, 'Trân châu đen', 'g', 2970, 500),
  (8, 'NL008', 4, 'Thạch cà phê', 'g', 2970, 500),
  (9, 'NL009', 3, 'Bột Freeze trà xanh', 'g', 3960, 1000),
  (10, 'NL010', 6, 'Bánh mì que', 'cái', 99, 100),
  (11, 'NL011', 6, 'Bánh Tiramisu', 'cái', 49, 10);

INSERT IGNORE INTO san_pham (id, ma_san_pham, danh_muc_id, ten_san_pham, mo_ta) VALUES
 (1, 'SP001', 1, 'Phin Sữa Đá',     'Cà phê phin pha với sữa đặc, uống lạnh'),
 (2, 'SP002', 1, 'Phin Đen Đá',     'Cà phê phin đen, uống lạnh'),
 (3, 'SP003', 1, 'Bạc Xỉu',         'Cà phê nhiều sữa'),
 (4, 'SP004', 2, 'Trà Đen Sữa',     'Trà đen pha sữa tươi'),
 (5, 'SP005', 3, 'Freeze Trà Xanh', 'Đá xay vị trà xanh'),
 (6, 'SP006', 4, 'Bánh Mì Que',     'Bánh mì que nhân pate'),
 (7, 'SP007', 4, 'Bánh Tiramisu',   'Bánh ngọt vị cà phê');

-- Giá (minh họa) theo từng size. Bánh dùng size STD (id 4).
INSERT IGNORE INTO san_pham_kich_thuoc (id, san_pham_id, kich_thuoc_id, gia_ban) VALUES
 (1, 1,1,29000),(2, 1,2,35000),(3, 1,3,45000),
 (4, 2,1,25000),(5, 2,2,29000),(6, 2,3,39000),
 (7, 3,1,29000),(8, 3,2,39000),(9, 3,3,45000),
 (10,4,1,35000),(11,4,2,45000),(12,4,3,55000),
 (13,5,1,45000),(14,5,2,55000),(15,5,3,65000),
 (16,6,4,19000),
 (17,7,4,39000);

INSERT IGNORE INTO topping (id, ten_topping, gia_ban, nguyen_lieu_id, luong_su_dung) VALUES
 (1, 'Trân châu đen',  10000, 7, 30),
 (2, 'Thạch cà phê',   10000, 8, 30),
 (3, 'Thêm shot cà phê', 10000, 1, 10);

-- Công thức cho 1 phần SIZE M. Size S/L nhân với kich_thuoc.he_so_dinh_muc.
INSERT IGNORE INTO cong_thuc (id, san_pham_id, nguyen_lieu_id, so_luong) VALUES
 (1, 1,1,20),(2, 1,2,30),(3, 1,3,150),                 -- Phin Sữa Đá: cà phê, sữa đặc, đá
 (4, 2,1,20),(5, 2,3,150),                              -- Phin Đen Đá
 (6, 3,1,12),(7, 3,2,25),(8, 3,4,60),(9, 3,3,120),     -- Bạc Xỉu
 (10,4,5,8),(11,4,4,80),(12,4,6,20),(13,4,3,130),      -- Trà Đen Sữa
 (14,5,9,40),(15,5,4,60),(16,5,3,200),(17,5,6,15),     -- Freeze Trà Xanh
 (18,6,10,1),                                           -- Bánh Mì Que
 (19,7,11,1);                                           -- Bánh Tiramisu

-- 3.3 Nhà cung cấp, phiếu nhập (bảng nguyen_lieu đã nạp ở trên, tồn = kết quả sổ kho) --

INSERT IGNORE INTO nha_cung_cap (id, ma_nha_cung_cap, ten_nha_cung_cap, nguoi_lien_he, so_dien_thoai, email, dia_chi) VALUES
 (1, 'NCC001', 'Công ty Cà phê Demo A',  'Anh Tâm',  '0920000001', 'tam.demo@example.com',  'Hà Nội'),
 (2, 'NCC002', 'Công ty Thực phẩm Demo B','Chị Hương','0920000002', 'huong.demo@example.com','Hà Nội'),
 (3, 'NCC003', 'Cơ sở Bánh Demo C',      'Anh Long', '0920000003', NULL,                    'Hà Nội');

-- nguoi_tao_id / nguoi_duyet_id là tai_khoan.id (2 = thungan01, 1 = quanly01)
INSERT IGNORE INTO phieu_nhap (id, ma_phieu_nhap, nha_cung_cap_id, nguoi_tao_id, nguoi_duyet_id, ngay_nhap, tong_tien, trang_thai, ghi_chu) VALUES
 (1, 'PN20261020001', 1, 2, 1, '2026-10-20', 3000000, 'DA_NHAP_KHO', 'Nhập cà phê, sữa, trà'),
 (2, 'PN20261021001', 2, 2, 1, '2026-10-21', 3050000, 'DA_NHAP_KHO', 'Nhập đá, siro, topping, bột, bánh'),
 (3, 'PN20261030001', 1, 2, NULL, '2026-10-30', 900000, 'CHO_DUYET',  'Phiếu chờ quản lý duyệt (chưa cộng kho)');

INSERT IGNORE INTO chi_tiet_phieu_nhap (id, phieu_nhap_id, nguyen_lieu_id, so_luong, don_gia) VALUES
 (1, 1, 1,  5000, 300), (2, 1, 2, 10000, 60), (3, 1, 4, 20000, 25), (4, 1, 5, 2000, 200),
 (5, 2, 3,100000,   1), (6, 2, 6,  5000, 40), (7, 2, 7,  3000, 80), (8, 2, 8, 3000, 70),
 (9, 2, 9,  4000, 150), (10,2,10,   100,6000), (11,2,11,    50,22000),
 (12,3, 1,  3000, 300);

-- 3.4 Khuyến mãi -------------------------------------------------------
INSERT IGNORE INTO khuyen_mai (id, ten_khuyen_mai, ma_code, loai_giam, gia_tri_giam, giam_toi_da, don_toi_thieu, ngay_bat_dau, ngay_ket_thuc, so_luot_toi_da, so_luot_da_dung, trang_thai) VALUES
 (1, 'Giảm 10% đơn từ 100.000đ', 'GIAM10',  'PHAN_TRAM', 10, 20000, 100000, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 500, 1, 'HOAT_DONG'),
 (2, 'Giảm 5.000đ đơn từ 50.000đ', NULL,    'SO_TIEN',   5000, NULL, 50000, '2026-10-01 00:00:00', '2026-12-31 23:59:59', NULL, 0, 'HOAT_DONG'),
 (3, 'Khuyến mãi hè 2026 (đã hết hạn)', 'HE2026', 'PHAN_TRAM', 15, 30000, 0, '2026-06-01 00:00:00', '2026-08-31 23:59:59', NULL, 0, 'TAM_DUNG');

-- 3.5 Đơn hàng ---------------------------------------------------------
-- DH1 hoàn thành, tại quầy, khách vãng lai, tiền mặt
-- DH2 hoàn thành, online giao hàng, dùng mã GIAM10 + 20 điểm, chuyển khoản
-- DH3 hoàn thành, tại quầy, khách thành viên, tiền mặt
-- DH4 ĐÃ HỦY sau khi đã xuất kho  -> minh họa hoàn kho / hoàn tiền
-- DH5 mới tạo (online, chưa thanh toán)
-- DH6 đang chuẩn bị (đã thanh toán, đã xuất kho)
-- tai_khoan_id = tài khoản nhân viên xử lý đơn (2 = thungan01); NULL = đơn online chưa có người nhận
INSERT IGNORE INTO don_hang (id, ma_don_hang, khach_hang_id, tai_khoan_id, khuyen_mai_id, kenh_dat, hinh_thuc_phuc_vu, trang_thai,
    ten_nguoi_nhan, so_dien_thoai_nhan, dia_chi_giao_hang, ghi_chu,
    tong_tien_hang, tien_giam_khuyen_mai, diem_su_dung, tien_giam_diem, tong_thanh_toan,
    thoi_gian_dat, thoi_gian_hoan_thanh, thoi_gian_huy, ly_do_huy) VALUES
 (1, 'DH20261028001', NULL, 2, NULL, 'TAI_QUAY', 'TAI_CHO',  'HOAN_THANH', NULL, NULL, NULL, 'Bàn gần cửa sổ',
     89000, 0, 0, 0, 89000, '2026-10-28 08:15:00', '2026-10-28 08:26:00', NULL, NULL),
 (2, 'DH20261028002', 1, 2, 1, 'ONLINE', 'GIAO_HANG', 'HOAN_THANH', 'Đỗ Thị Lan', '0910000001', 'Tòa Lâm Viên, 107 Nguyễn Phong Sắc, Cầu Giấy, Hà Nội', 'Giao giờ nghỉ trưa',
     120000, 12000, 20, 2000, 106000, '2026-10-28 12:00:00', '2026-10-28 12:40:00', NULL, NULL),
 (3, 'DH20261029001', 2, 2, NULL, 'TAI_QUAY', 'MANG_DI', 'HOAN_THANH', NULL, NULL, NULL, NULL,
     84000, 0, 0, 0, 84000, '2026-10-29 14:30:00', '2026-10-29 14:43:00', NULL, NULL),
 (4, 'DH20261030001', 3, 2, NULL, 'ONLINE', 'MANG_DI', 'DA_HUY', 'Bùi Ngọc Mai', '0910000003', NULL, NULL,
     58000, 0, 0, 0, 58000, '2026-10-30 09:00:00', NULL, '2026-10-30 09:20:00', 'Khách đổi ý, không đến lấy'),
 (5, 'DH20261031001', 1, NULL, NULL, 'ONLINE', 'MANG_DI', 'MOI_TAO', 'Đỗ Thị Lan', '0910000001', NULL, NULL,
     29000, 0, 0, 0, 29000, '2026-10-31 10:00:00', NULL, NULL, NULL),
 (6, 'DH20261031002', NULL, 2, NULL, 'TAI_QUAY', 'TAI_CHO', 'DANG_CHUAN_BI', NULL, NULL, NULL, NULL,
     55000, 0, 0, 0, 55000, '2026-10-31 11:00:00', NULL, NULL, NULL);

-- Chi tiết: don_gia = giá size TẠI THỜI ĐIỂM ĐẶT (chụp lại, không đổi khi menu đổi giá)
INSERT IGNORE INTO chi_tiet_don_hang (id, don_hang_id, san_pham_id, kich_thuoc_id, ten_san_pham, don_gia, so_luong, muc_duong, muc_da, thanh_tien) VALUES
 (1, 1, 1, 2, 'Phin Sữa Đá',     35000, 2, '100%', '100%', 70000),
 (2, 1, 6, 4, 'Bánh Mì Que',     19000, 1, NULL, NULL,     19000),
 (3, 2, 3, 3, 'Bạc Xỉu',         45000, 1, '70%',  '50%',  55000),
 (4, 2, 5, 2, 'Freeze Trà Xanh', 55000, 1, NULL, NULL,     65000),
 (5, 3, 4, 2, 'Trà Đen Sữa',     45000, 1, '50%',  '100%', 45000),
 (6, 3, 7, 4, 'Bánh Tiramisu',   39000, 1, NULL, NULL,     39000),
 (7, 4, 2, 2, 'Phin Đen Đá',     29000, 2, '100%', '100%', 58000),
 (8, 5, 1, 1, 'Phin Sữa Đá',     29000, 1, '100%', '100%', 29000),
 (9, 6, 1, 3, 'Phin Sữa Đá',     45000, 1, '100%', '100%', 55000);

INSERT IGNORE INTO chi_tiet_don_hang_topping (id, chi_tiet_don_hang_id, topping_id, so_luong, don_gia) VALUES
 (1, 3, 1, 1, 10000),   -- Bạc Xỉu L + Trân châu
 (2, 4, 2, 1, 10000),   -- Freeze + Thạch cà phê
 (3, 9, 3, 1, 10000);   -- Phin Sữa Đá L + shot cà phê

-- 3.6 Hóa đơn và thanh toán (DH5 chưa có hóa đơn vì chưa thanh toán) -------
INSERT IGNORE INTO hoa_don (id, ma_hoa_don, don_hang_id, tai_khoan_id, thoi_gian_lap, so_tien_phai_tra, trang_thai) VALUES
 (1, 'HD20261028001', 1, 2, '2026-10-28 08:18:00',  89000, 'DA_THANH_TOAN'),
 (2, 'HD20261028002', 2, 2, '2026-10-28 12:03:00', 106000, 'DA_THANH_TOAN'),
 (3, 'HD20261029001', 3, 2, '2026-10-29 14:35:00',  84000, 'DA_THANH_TOAN'),
 (4, 'HD20261030001', 4, 2, '2026-10-30 09:03:00',  58000, 'DA_HOAN_TIEN'),
 (5, 'HD20261031001', 6, 2, '2026-10-31 11:03:00',  55000, 'DA_THANH_TOAN');

INSERT IGNORE INTO thanh_toan (id, hoa_don_id, tai_khoan_id, phuong_thuc, so_tien, so_tien_khach_dua, tien_thua, ma_giao_dich, trang_thai, thoi_gian_thanh_toan) VALUES
 (1, 1, 2, 'TIEN_MAT',     89000, 100000, 11000, NULL,            'THANH_CONG', '2026-10-28 08:18:00'),
 (2, 2, 2, 'CHUYEN_KHOAN', 106000, NULL, NULL,  'CK20261028001', 'THANH_CONG', '2026-10-28 12:03:00'),
 (3, 3, 2, 'TIEN_MAT',     84000, 100000, 16000, NULL,            'THANH_CONG', '2026-10-29 14:35:00'),
 (4, 4, 2, 'CHUYEN_KHOAN', 58000, NULL, NULL,   'CK20261030001', 'DA_HOAN',    '2026-10-30 09:03:00'),
 (5, 5, 2, 'TIEN_MAT',     55000, 55000, 0,     NULL,            'THANH_CONG', '2026-10-31 11:03:00');

-- 3.7 Nhật ký trạng thái đơn ---------------------------------------------
INSERT IGNORE INTO lich_su_trang_thai_don_hang (id, don_hang_id, trang_thai_cu, trang_thai_moi, tai_khoan_id, ghi_chu, created_at) VALUES
 (1, 1, NULL,'MOI_TAO',2,NULL,'2026-10-28 08:15:00'),(2, 1,'MOI_TAO','DA_XAC_NHAN',2,'Đã nhận tiền mặt','2026-10-28 08:18:00'),
 (3, 1,'DA_XAC_NHAN','DANG_CHUAN_BI',3,'Trừ kho','2026-10-28 08:20:00'),(4, 1,'DANG_CHUAN_BI','SAN_SANG',3,NULL,'2026-10-28 08:25:00'),
 (5, 1,'SAN_SANG','HOAN_THANH',2,NULL,'2026-10-28 08:26:00'),
 (6, 2, NULL,'MOI_TAO',6,'Khách đặt online','2026-10-28 12:00:00'),(7, 2,'MOI_TAO','DA_XAC_NHAN',2,'Đã nhận chuyển khoản','2026-10-28 12:03:00'),
 (8, 2,'DA_XAC_NHAN','DANG_CHUAN_BI',3,'Trừ kho','2026-10-28 12:05:00'),(9, 2,'DANG_CHUAN_BI','SAN_SANG',3,NULL,'2026-10-28 12:15:00'),
 (10,2,'SAN_SANG','HOAN_THANH',5,'Đã giao','2026-10-28 12:40:00'),
 (11,3, NULL,'MOI_TAO',2,NULL,'2026-10-29 14:30:00'),(12,3,'MOI_TAO','DA_XAC_NHAN',2,NULL,'2026-10-29 14:35:00'),
 (13,3,'DA_XAC_NHAN','DANG_CHUAN_BI',3,'Trừ kho','2026-10-29 14:36:00'),(14,3,'DANG_CHUAN_BI','SAN_SANG',3,NULL,'2026-10-29 14:42:00'),
 (15,3,'SAN_SANG','HOAN_THANH',2,NULL,'2026-10-29 14:43:00'),
 (16,4, NULL,'MOI_TAO',8,'Khách đặt online','2026-10-30 09:00:00'),(17,4,'MOI_TAO','DA_XAC_NHAN',2,'Đã nhận chuyển khoản','2026-10-30 09:03:00'),
 (18,4,'DA_XAC_NHAN','DANG_CHUAN_BI',4,'Trừ kho','2026-10-30 09:05:00'),
 (19,4,'DANG_CHUAN_BI','DA_HUY',1,'Khách đổi ý - hoàn kho, hoàn tiền','2026-10-30 09:20:00'),
 (20,5, NULL,'MOI_TAO',6,'Khách đặt online','2026-10-31 10:00:00'),
 (21,6, NULL,'MOI_TAO',2,NULL,'2026-10-31 11:00:00'),(22,6,'MOI_TAO','DA_XAC_NHAN',2,'Đã nhận tiền mặt','2026-10-31 11:03:00'),
 (23,6,'DA_XAC_NHAN','DANG_CHUAN_BI',3,'Trừ kho','2026-10-31 11:05:00');

-- 3.8 Sổ kho: nhập (từ phiếu nhập), xuất bán hàng, hoàn kho, hao hụt ---------
INSERT IGNORE INTO giao_dich_kho (id, nguyen_lieu_id, loai_giao_dich, so_luong_thay_doi, ton_sau_giao_dich, don_gia, chi_tiet_phieu_nhap_id, don_hang_id, giao_dich_goc_id, tai_khoan_id, ly_do, created_at) VALUES
  (1, 1, 'NHAP_KHO', 5000, 5000, 300, 1, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-20 09:00:00'),
  (2, 2, 'NHAP_KHO', 10000, 10000, 60, 2, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-20 09:00:00'),
  (3, 4, 'NHAP_KHO', 20000, 20000, 25, 3, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-20 09:00:00'),
  (4, 5, 'NHAP_KHO', 2000, 2000, 200, 4, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-20 09:00:00'),
  (5, 3, 'NHAP_KHO', 100000, 100000, 1, 5, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-21 08:30:00'),
  (6, 6, 'NHAP_KHO', 5000, 5000, 40, 6, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-21 08:30:00'),
  (7, 7, 'NHAP_KHO', 3000, 3000, 80, 7, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-21 08:30:00'),
  (8, 8, 'NHAP_KHO', 3000, 3000, 70, 8, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-21 08:30:00'),
  (9, 9, 'NHAP_KHO', 4000, 4000, 150, 9, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-21 08:30:00'),
  (10, 10, 'NHAP_KHO', 100, 100, 6000, 10, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-21 08:30:00'),
  (11, 11, 'NHAP_KHO', 50, 50, 22000, 11, NULL, NULL, 1, 'Nhập kho theo phiếu nhập', '2026-10-21 08:30:00'),
  (12, 1, 'XUAT_BAN_HANG', -40, 4960, NULL, NULL, 1, NULL, 2, NULL, '2026-10-28 08:20:00'),
  (13, 2, 'XUAT_BAN_HANG', -60, 9940, NULL, NULL, 1, NULL, 2, NULL, '2026-10-28 08:20:00'),
  (14, 3, 'XUAT_BAN_HANG', -300, 99700, NULL, NULL, 1, NULL, 2, NULL, '2026-10-28 08:20:00'),
  (15, 10, 'XUAT_BAN_HANG', -1, 99, NULL, NULL, 1, NULL, 2, NULL, '2026-10-28 08:20:00'),
  (16, 1, 'XUAT_BAN_HANG', -15.6, 4944.4, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (17, 2, 'XUAT_BAN_HANG', -32.5, 9907.5, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (18, 3, 'XUAT_BAN_HANG', -356, 99344, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (19, 4, 'XUAT_BAN_HANG', -138, 19862, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (20, 6, 'XUAT_BAN_HANG', -15, 4985, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (21, 7, 'XUAT_BAN_HANG', -30, 2970, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (22, 8, 'XUAT_BAN_HANG', -30, 2970, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (23, 9, 'XUAT_BAN_HANG', -40, 3960, NULL, NULL, 2, NULL, 3, NULL, '2026-10-28 12:05:00'),
  (24, 3, 'XUAT_BAN_HANG', -130, 99214, NULL, NULL, 3, NULL, 3, NULL, '2026-10-29 14:36:00'),
  (25, 4, 'XUAT_BAN_HANG', -80, 19782, NULL, NULL, 3, NULL, 3, NULL, '2026-10-29 14:36:00'),
  (26, 5, 'XUAT_BAN_HANG', -8, 1992, NULL, NULL, 3, NULL, 3, NULL, '2026-10-29 14:36:00'),
  (27, 6, 'XUAT_BAN_HANG', -20, 4965, NULL, NULL, 3, NULL, 3, NULL, '2026-10-29 14:36:00'),
  (28, 11, 'XUAT_BAN_HANG', -1, 49, NULL, NULL, 3, NULL, 3, NULL, '2026-10-29 14:36:00'),
  (29, 1, 'XUAT_BAN_HANG', -40, 4904.4, NULL, NULL, 4, NULL, 4, NULL, '2026-10-30 09:05:00'),
  (30, 3, 'XUAT_BAN_HANG', -300, 98914, NULL, NULL, 4, NULL, 4, NULL, '2026-10-30 09:05:00'),
  (31, 1, 'HOAN_KHO', 40, 4944.4, NULL, NULL, 4, 29, 1, 'Hủy đơn DH20261030001 - hoàn nguyên liệu đã xuất', '2026-10-30 09:20:00'),
  (32, 3, 'HOAN_KHO', 300, 99214, NULL, NULL, 4, 30, 1, 'Hủy đơn DH20261030001 - hoàn nguyên liệu đã xuất', '2026-10-30 09:20:00'),
  (33, 4, 'XUAT_HAO_HUT', -500, 19282, NULL, NULL, NULL, NULL, 1, 'Sữa tươi hết hạn sử dụng, loại bỏ', '2026-10-30 18:00:00'),
  (34, 1, 'XUAT_BAN_HANG', -36, 4908.4, NULL, NULL, 6, NULL, 3, NULL, '2026-10-31 11:05:00'),
  (35, 2, 'XUAT_BAN_HANG', -39, 9868.5, NULL, NULL, 6, NULL, 3, NULL, '2026-10-31 11:05:00'),
  (36, 3, 'XUAT_BAN_HANG', -195, 99019, NULL, NULL, 6, NULL, 3, NULL, '2026-10-31 11:05:00');

-- 3.9 Điểm thưởng (khớp khach_hang.diem_tich_luy: KH001=40, KH002=8, KH003=0) -
INSERT IGNORE INTO lich_su_diem (id, khach_hang_id, don_hang_id, loai_giao_dich, so_diem, diem_sau_giao_dich, ghi_chu, created_at) VALUES
 (1, 1, NULL, 'DIEU_CHINH', 50, 50, 'Tặng điểm chào mừng thành viên mới', '2026-10-25 10:00:00'),
 (2, 1, 2,    'SU_DUNG',   -20, 30, 'Dùng 20 điểm cho đơn DH20261028002', '2026-10-28 12:00:00'),
 (3, 1, 2,    'TICH_DIEM',  10, 40, 'Tích điểm đơn DH20261028002 (1 điểm / 10.000đ)', '2026-10-28 12:40:00'),
 (4, 2, 3,    'TICH_DIEM',   8,  8, 'Tích điểm đơn DH20261029001', '2026-10-29 14:43:00');

-- 3.10 Đánh giá / phản hồi ------------------------------------------------
INSERT IGNORE INTO danh_gia_phan_hoi (id, khach_hang_id, san_pham_id, so_sao, noi_dung) VALUES
 (1, 1, 3, 5, 'Bạc xỉu ngon, vừa ngọt.'),
 (2, 2, 4, 4, 'Trà thơm, hơi nhiều đá.'),
 (3, 1, NULL, NULL, 'Giao hàng nhanh, nhân viên nhiệt tình.');

/* ---------------------------------------------------------------------
   PHẦN 4 – KIỂM TRA SAU KHI CHẠY  (chạy bao nhiêu lần cũng được)
   Cách dùng trong MySQL Workbench: bôi đen TỪNG câu SELECT rồi bấm Ctrl+Enter.
   Kết quả hiện ở bảng "Result Grid" phía dưới.
   --------------------------------------------------------------------- */
USE highlands_coffee;

-- 4.0 Đếm số bảng/view: kỳ vọng BASE TABLE = 23, VIEW = 4
SELECT TABLE_TYPE, COUNT(*) AS so_luong
FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'highlands_coffee' GROUP BY TABLE_TYPE;

-- 4.1 Danh sách tài khoản (kỳ vọng 8 dòng; KHÔNG hiện mật khẩu)
SELECT id, ten_dang_nhap, vai_tro, trang_thai FROM tai_khoan ORDER BY id;

-- 4.2 Tài khoản nội bộ được phép bán hàng / xử lý đơn (kỳ vọng 5 dòng: 1 QUAN_LY + 4 NHAN_VIEN)
SELECT id, ten_dang_nhap, vai_tro, trang_thai
FROM tai_khoan
WHERE vai_tro IN ('QUAN_LY','NHAN_VIEN')
ORDER BY id;

-- 4.3 Danh sách sản phẩm + giá theo size (kỳ vọng 17 dòng)
SELECT sp.ma_san_pham, sp.ten_san_pham, dm.ten_danh_muc, kt.ma_kich_thuoc, spkt.gia_ban
FROM san_pham sp
JOIN danh_muc dm            ON dm.id = sp.danh_muc_id
JOIN san_pham_kich_thuoc spkt ON spkt.san_pham_id = sp.id
JOIN kich_thuoc kt          ON kt.id = spkt.kich_thuoc_id
ORDER BY sp.id, kt.he_so_dinh_muc;

-- 4.4 Danh sách đơn hàng (kỳ vọng 6 dòng: 3 hoàn thành, 1 hủy, 1 mới, 1 đang chuẩn bị)
SELECT dh.ma_don_hang, COALESCE(kh.ho_ten, 'Khách vãng lai') AS khach_hang,
       dh.kenh_dat, dh.hinh_thuc_phuc_vu, dh.trang_thai,
       dh.tong_tien_hang, dh.tien_giam_khuyen_mai + dh.tien_giam_diem AS giam_gia, dh.tong_thanh_toan
FROM don_hang dh LEFT JOIN khach_hang kh ON kh.id = dh.khach_hang_id
ORDER BY dh.thoi_gian_dat;

-- 4.5 Chi tiết từng đơn hàng (dùng view)
SELECT * FROM v_chi_tiet_don_hang ORDER BY don_hang_id, chi_tiet_id;

-- 4.5b Kiểm tra tổng đơn = tổng các dòng (kỳ vọng 0 dòng = không lệch)
SELECT dh.ma_don_hang, dh.tong_tien_hang, SUM(ct.thanh_tien) AS tong_dong
FROM don_hang dh JOIN chi_tiet_don_hang ct ON ct.don_hang_id = dh.id
GROUP BY dh.id, dh.ma_don_hang, dh.tong_tien_hang
HAVING dh.tong_tien_hang <> SUM(ct.thanh_tien);

-- 4.6 Doanh thu theo ngày (chỉ đơn HOAN_THANH; kỳ vọng 28/10 = 195.000 (2 đơn), 29/10 = 84.000)
SELECT * FROM v_doanh_thu_theo_ngay ORDER BY ngay;

-- 4.7 Sản phẩm bán chạy
SELECT * FROM v_san_pham_ban_chay ORDER BY so_luong_ban DESC, doanh_thu DESC;

-- 4.8 Số lượng nguyên liệu tồn kho
SELECT nl.ma_nguyen_lieu, nl.ten_nguyen_lieu, nhom.ten_nhom, nl.so_luong_ton, nl.don_vi_tinh, nl.muc_ton_toi_thieu
FROM nguyen_lieu nl JOIN nhom_nguyen_lieu nhom ON nhom.id = nl.nhom_nguyen_lieu_id
ORDER BY nl.id;

-- 4.9 Nguyên liệu dưới mức tồn tối thiểu (kỳ vọng: Bánh mì que, còn 99 < 100)
SELECT * FROM v_nguyen_lieu_duoi_muc_toi_thieu;

-- 4.10 Lịch sử nhập và xuất kho
SELECT g.id, g.created_at, nl.ten_nguyen_lieu, g.loai_giao_dich, g.so_luong_thay_doi, g.ton_sau_giao_dich,
       dh.ma_don_hang, g.giao_dich_goc_id, g.ly_do
FROM giao_dich_kho g
JOIN nguyen_lieu nl  ON nl.id = g.nguyen_lieu_id
LEFT JOIN don_hang dh ON dh.id = g.don_hang_id
ORDER BY g.id;

-- 4.10b Tồn hiện tại phải = tổng sổ kho (kỳ vọng 0 dòng = khớp)
SELECT nl.ma_nguyen_lieu, nl.so_luong_ton, SUM(g.so_luong_thay_doi) AS tong_so_kho
FROM nguyen_lieu nl JOIN giao_dich_kho g ON g.nguyen_lieu_id = nl.id
GROUP BY nl.id, nl.ma_nguyen_lieu, nl.so_luong_ton
HAVING nl.so_luong_ton <> SUM(g.so_luong_thay_doi);

-- 4.10c Đơn đã hủy: mỗi dòng xuất phải có đúng 1 dòng hoàn (kỳ vọng 0 dòng = đủ, không hoàn thiếu/thừa)
SELECT x.id AS giao_dich_xuat, x.don_hang_id, COUNT(h.id) AS so_lan_hoan
FROM giao_dich_kho x
JOIN don_hang dh ON dh.id = x.don_hang_id AND dh.trang_thai = 'DA_HUY'
LEFT JOIN giao_dich_kho h ON h.giao_dich_goc_id = x.id
WHERE x.loai_giao_dich = 'XUAT_BAN_HANG'
GROUP BY x.id, x.don_hang_id
HAVING COUNT(h.id) <> 1;

-- 4.11 Lịch sử điểm thưởng khách hàng
SELECT kh.ma_khach_hang, kh.ho_ten, l.created_at, l.loai_giao_dich, l.so_diem, l.diem_sau_giao_dich, l.ghi_chu
FROM lich_su_diem l JOIN khach_hang kh ON kh.id = l.khach_hang_id
ORDER BY l.khach_hang_id, l.id;

-- 4.11b Điểm hiện tại phải khớp sổ điểm (kỳ vọng 0 dòng = khớp)
SELECT kh.ma_khach_hang, kh.diem_tich_luy, COALESCE(SUM(l.so_diem),0) AS tong_so_diem
FROM khach_hang kh LEFT JOIN lich_su_diem l ON l.khach_hang_id = kh.id
GROUP BY kh.id, kh.ma_khach_hang, kh.diem_tich_luy
HAVING kh.diem_tich_luy <> COALESCE(SUM(l.so_diem),0);

-- 4.12 Công thức pha chế của 1 món + lượng nguyên liệu cho 1 ly SIZE L (ví dụ Phin Sữa Đá)
SELECT sp.ten_san_pham, nl.ten_nguyen_lieu, ct.so_luong AS dinh_muc_size_M,
       ROUND(ct.so_luong * kt.he_so_dinh_muc, 3) AS cho_1_ly_size_L, nl.don_vi_tinh
FROM cong_thuc ct
JOIN san_pham sp  ON sp.id = ct.san_pham_id
JOIN nguyen_lieu nl ON nl.id = ct.nguyen_lieu_id
JOIN kich_thuoc kt ON kt.ma_kich_thuoc = 'L'
WHERE sp.ma_san_pham = 'SP001';

-- 4.13 Ai xử lý đơn, lập hóa đơn, thu tiền (thay cho nhan_vien cũ; dùng tai_khoan)
SELECT dh.ma_don_hang, dh.trang_thai,
       COALESCE(tk_dh.ten_dang_nhap, '(chưa có người nhận)') AS nguoi_xu_ly_don,
       hd.ma_hoa_don, tk_hd.ten_dang_nhap AS nguoi_lap_hoa_don,
       tk_tt.ten_dang_nhap AS nguoi_thu_tien
FROM don_hang dh
LEFT JOIN tai_khoan tk_dh ON tk_dh.id = dh.tai_khoan_id
LEFT JOIN hoa_don hd      ON hd.don_hang_id = dh.id
LEFT JOIN tai_khoan tk_hd ON tk_hd.id = hd.tai_khoan_id
LEFT JOIN thanh_toan tt   ON tt.hoa_don_id = hd.id
LEFT JOIN tai_khoan tk_tt ON tk_tt.id = tt.tai_khoan_id
ORDER BY dh.id;

-- 4.14 Phiếu nhập: người lập và người duyệt (tai_khoan)
SELECT pn.ma_phieu_nhap, pn.trang_thai,
       tk1.ten_dang_nhap AS nguoi_tao, tk2.ten_dang_nhap AS nguoi_duyet
FROM phieu_nhap pn
JOIN tai_khoan tk1 ON tk1.id = pn.nguoi_tao_id
LEFT JOIN tai_khoan tk2 ON tk2.id = pn.nguoi_duyet_id
ORDER BY pn.id;

-- 4.15 Báo cáo số đơn đã xử lý theo tài khoản nhân viên (chỉ đơn HOAN_THANH)
SELECT tk.ten_dang_nhap, COUNT(*) AS so_don_hoan_thanh, SUM(dh.tong_thanh_toan) AS doanh_thu
FROM don_hang dh JOIN tai_khoan tk ON tk.id = dh.tai_khoan_id
WHERE dh.trang_thai = 'HOAN_THANH'
GROUP BY tk.id, tk.ten_dang_nhap;

-- 4.16 KIỂM TRA KHÔNG CÒN BẢNG NHÂN SỰ (kỳ vọng 0 dòng)
SELECT TABLE_NAME FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'highlands_coffee'
  AND TABLE_NAME IN ('chuc_vu','nhan_vien','ca_lam_viec','lich_lam_viec','cham_cong');

-- 4.17 KIỂM TRA KHÔNG CÒN KHÓA NGOẠI TRỎ TỚI BẢNG ĐÃ XÓA (kỳ vọng 0 dòng)
SELECT TABLE_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'highlands_coffee'
  AND REFERENCED_TABLE_NAME IN ('chuc_vu','nhan_vien','ca_lam_viec','lich_lam_viec','cham_cong');

-- 4.18 KIỂM TRA KHÔNG CÒN CỘT NHAN_VIEN_ID / CHUC_VU_ID... (kỳ vọng 0 dòng)
SELECT TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'highlands_coffee'
  AND COLUMN_NAME IN ('nhan_vien_id','chuc_vu_id','ca_lam_viec_id','lich_lam_viec_id','nguoi_phan_cong_id');

-- 4.19 Liệt kê các khóa ngoại trỏ tới tai_khoan (kỳ vọng 8 dòng)
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'highlands_coffee' AND REFERENCED_TABLE_NAME = 'tai_khoan'
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 4.20 Người thực hiện phải là QUAN_LY hoặc NHAN_VIEN, không phải KHACH_HANG (kỳ vọng 0 dòng)
-- (MySQL không cho CHECK xuyên bảng, nên kiểm tra bằng truy vấn; ứng dụng cũng cần kiểm tra khi ghi)
SELECT 'don_hang' AS bang, dh.id FROM don_hang dh JOIN tai_khoan tk ON tk.id = dh.tai_khoan_id WHERE tk.vai_tro = 'KHACH_HANG'
UNION ALL SELECT 'hoa_don', x.id FROM hoa_don x JOIN tai_khoan tk ON tk.id = x.tai_khoan_id WHERE tk.vai_tro = 'KHACH_HANG'
UNION ALL SELECT 'thanh_toan', x.id FROM thanh_toan x JOIN tai_khoan tk ON tk.id = x.tai_khoan_id WHERE tk.vai_tro = 'KHACH_HANG'
UNION ALL SELECT 'phieu_nhap(nguoi_tao)', x.id FROM phieu_nhap x JOIN tai_khoan tk ON tk.id = x.nguoi_tao_id WHERE tk.vai_tro = 'KHACH_HANG'
UNION ALL SELECT 'phieu_nhap(nguoi_duyet)', x.id FROM phieu_nhap x JOIN tai_khoan tk ON tk.id = x.nguoi_duyet_id WHERE tk.vai_tro = 'KHACH_HANG'
UNION ALL SELECT 'giao_dich_kho', x.id FROM giao_dich_kho x JOIN tai_khoan tk ON tk.id = x.tai_khoan_id WHERE tk.vai_tro = 'KHACH_HANG';

/* ---------------------------------------------------------------------
   PHẦN 5 – (TÙY CHỌN) THỬ RÀNG BUỘC: MỖI CÂU DƯỚI ĐÂY PHẢI BÁO LỖI
   Đang để trong chú thích. Muốn thử: bỏ dấu "-- " ở đầu 1 câu rồi chạy.
   Lỗi báo ra nghĩa là ràng buộc đang bảo vệ dữ liệu đúng.
   Không câu nào làm thay đổi dữ liệu vì đều bị từ chối.
   --------------------------------------------------------------------- */
-- a) Xuất vượt tồn (tồn Sữa đặc ~9.868 g)  -> lỗi Check constraint 'chk_nguyen_lieu_ton'
-- UPDATE nguyen_lieu SET so_luong_ton = so_luong_ton - 999999 WHERE ma_nguyen_lieu = 'NL002';

-- b) Hoàn kho LẦN HAI cho cùng 1 lần xuất (xuất của đơn 4 đã được hoàn)
--    -> lỗi Duplicate entry ... for key 'uq_gd_kho_goc'
-- INSERT INTO giao_dich_kho (nguyen_lieu_id, loai_giao_dich, so_luong_thay_doi, ton_sau_giao_dich, don_hang_id, giao_dich_goc_id)
-- SELECT nguyen_lieu_id, 'HOAN_KHO', 1, 1, NULL, id FROM giao_dich_kho WHERE don_hang_id = 4 AND loai_giao_dich = 'XUAT_BAN_HANG' LIMIT 1;

-- c) Xuất kho 2 lần cho cùng đơn + cùng nguyên liệu -> lỗi Duplicate ... 'uq_gd_kho_don_nl_loai'
-- INSERT INTO giao_dich_kho (nguyen_lieu_id, loai_giao_dich, so_luong_thay_doi, ton_sau_giao_dich, don_hang_id)
-- VALUES (1, 'XUAT_BAN_HANG', -1, 1, 6);

-- d) Xuất kho mà ghi số dương -> lỗi Check constraint 'chk_gd_kho_dau'
-- INSERT INTO giao_dich_kho (nguyen_lieu_id, loai_giao_dich, so_luong_thay_doi, ton_sau_giao_dich) VALUES (1, 'XUAT_HAO_HUT', 5, 1);

-- e) Xóa sản phẩm đã có trong đơn hàng -> lỗi Cannot delete or update a parent row (FK)
-- DELETE FROM san_pham WHERE id = 1;

-- f) Sai tổng tiền đơn (tổng thanh toán không bằng tiền hàng - giảm) -> lỗi 'chk_don_hang_tong'
-- UPDATE don_hang SET tong_thanh_toan = 1 WHERE id = 1;

-- g) Đơn giao hàng không có địa chỉ -> lỗi 'chk_don_hang_giao_hang'
-- UPDATE don_hang SET hinh_thuc_phuc_vu = 'GIAO_HANG' WHERE id = 1;

-- h) Một đơn có 2 hóa đơn -> lỗi Duplicate ... 'uq_hoa_don_don_hang'
-- INSERT INTO hoa_don (ma_hoa_don, don_hang_id, so_tien_phai_tra) VALUES ('HD-TEST', 1, 1);

-- i) Gán đơn cho tài khoản không tồn tại (thay cho nhân viên không tồn tại) -> lỗi FK 'fk_don_hang_tai_khoan'
-- UPDATE don_hang SET tai_khoan_id = 9999 WHERE id = 5;

-- j) Xóa tài khoản đã từng xử lý đơn / lập phiếu nhập -> lỗi Cannot delete or update a parent row (FK)
-- DELETE FROM tai_khoan WHERE id = 2;
