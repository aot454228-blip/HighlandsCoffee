package com.highlands.frontend.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.highlands.frontend.dto.LoginResponse;

/** Lớp duy nhất của Frontend nói chuyện với Backend qua HTTP. */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public LoginResponse dangNhap(String tenDangNhap, String matKhau) {
        try {
            String body = mapper.writeValueAsString(
                    Map.of("tenDangNhap", tenDangNhap, "matKhau", matKhau));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/login"))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), LoginResponse.class);
            }

            // Backend trả lỗi dạng {"message": "..."}
            JsonNode node = mapper.readTree(response.body());
            String thongBao = node.hasNonNull("message")
                    ? node.get("message").asText()
                    : "Lỗi máy chủ (mã " + response.statusCode() + ")";
            throw new ApiException(thongBao);

        } catch (IOException e) {
            throw new ApiException("Không kết nối được máy chủ (backend chưa chạy?)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Yêu cầu bị gián đoạn");
        }
    }
}