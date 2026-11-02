package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.highlands.backend.entity.KhachHang;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {

    // Không nhập từ khóa: lấy 50 khách đầu tiên theo tên
    List<KhachHang> findTop50ByOrderByHoTenAsc();

    // Tìm theo một phần họ tên, số điện thoại hoặc mã khách hàng
    @Query("""
            select k from KhachHang k
            where lower(k.hoTen) like lower(concat('%', :tuKhoa, '%'))
               or k.soDienThoai like concat('%', :tuKhoa, '%')
               or lower(k.maKhachHang) like lower(concat('%', :tuKhoa, '%'))
            order by k.hoTen asc
            """)
    List<KhachHang> timKiem(@Param("tuKhoa") String tuKhoa);
}
