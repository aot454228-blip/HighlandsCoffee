package com.highlands.backend.exception;

import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ của phần quản lý, mang theo mã HTTP (400 sai dữ liệu, 404 không tìm thấy). */
public class QuanLyException extends RuntimeException {

    private final HttpStatus trangThaiHttp;

    public QuanLyException(HttpStatus trangThaiHttp, String message) {
        super(message);
        this.trangThaiHttp = trangThaiHttp;
    }

    public static QuanLyException saiDuLieu(String message) {
        return new QuanLyException(HttpStatus.BAD_REQUEST, message);
    }

    public static QuanLyException khongTimThay(String message) {
        return new QuanLyException(HttpStatus.NOT_FOUND, message);
    }

    public HttpStatus getTrangThaiHttp() {
        return trangThaiHttp;
    }
}