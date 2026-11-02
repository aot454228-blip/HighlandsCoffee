package com.highlands.backend.controller;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.highlands.backend.exception.QuanLyException;

/** Trả lỗi của phần quản lý dạng {"message": "..."} (cùng dạng với API đăng nhập, đặt đơn). */
@RestControllerAdvice(assignableTypes = {
        DanhMucQuanLyController.class,
        SanPhamQuanLyController.class,
        ToppingQuanLyController.class,
        KhuyenMaiQuanLyController.class,
        ThongKeController.class })
public class QuanLyExceptionHandler {

    @ExceptionHandler(QuanLyException.class)
    public ResponseEntity<Map<String, String>> xuLyQuanLy(QuanLyException ex) {
        return ResponseEntity.status(ex.getTrangThaiHttp()).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> xuLyRangBuoc(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Dữ liệu vi phạm ràng buộc của cơ sở dữ liệu (trùng tên, trùng mã hoặc đang được sử dụng)"));
    }

    // JSON sai cú pháp, số hoặc ngày giờ sai định dạng
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Map<String, String>> xuLyDuLieuSai(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Dữ liệu gửi lên không hợp lệ (kiểm tra định dạng số, ngày yyyy-MM-dd, ngày giờ yyyy-MM-ddTHH:mm:ss)"));
    }
}