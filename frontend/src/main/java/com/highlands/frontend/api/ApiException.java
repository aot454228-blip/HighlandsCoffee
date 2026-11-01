package com.highlands.frontend.api;

/** Lỗi khi gọi API; message là nội dung sẽ hiện cho người dùng. */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}