package com.highlands.frontend.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.highlands.frontend.dto.DonHangDto;
import com.highlands.frontend.dto.DonHangTomTatDto;
import com.highlands.frontend.dto.HoaDonDto;
import com.highlands.frontend.dto.KhachHangDto;
import com.highlands.frontend.dto.LoginResponse;
import com.highlands.frontend.dto.SanPhamDto;
import com.highlands.frontend.dto.ToppingDto;
import com.highlands.frontend.model.MonTrongGio;

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

    public List<SanPhamDto> layThucDon() {
        return layDanhSach("/api/thuc-don", new TypeReference<List<SanPhamDto>>() { });
    }

    public List<ToppingDto> layTopping() {
        return layDanhSach("/api/topping", new TypeReference<List<ToppingDto>>() { });
    }

    /** Gửi giỏ hàng lên backend để tạo đơn. Không gửi giá: backend tự tra giá trong database. */
    public DonHangDto taoDonTaiQuay(Long taiKhoanId, String hinhThucPhucVu, String ghiChu,
            List<MonTrongGio> cacMon) {
        List<Map<String, Object>> dsMon = new ArrayList<>();
        for (MonTrongGio m : cacMon) {
            List<Long> idTopping = new ArrayList<>();
            for (ToppingDto t : m.getCacTopping()) {
                idTopping.add(t.id());
            }
            Map<String, Object> mon = new LinkedHashMap<>();
            mon.put("sanPhamId", m.getSanPhamId());
            mon.put("kichThuocId", m.getKichThuocId());
            mon.put("soLuong", m.getSoLuong());
            mon.put("cacToppingId", idTopping);
            mon.put("mucDuong", m.getMucDuong());
            mon.put("mucDa", m.getMucDa());
            mon.put("ghiChu", m.getGhiChu());
            dsMon.add(mon);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("taiKhoanId", taiKhoanId);
        body.put("hinhThucPhucVu", hinhThucPhucVu);
        body.put("ghiChu", ghiChu);
        body.put("cacMon", dsMon);
        return guiPost("/api/don-hang", body, DonHangDto.class);
    }

    /** Thanh toán một đơn. phuongThuc: TIEN_MAT (cần soTienKhachDua) hoặc CHUYEN_KHOAN (cần maGiaoDich). */
    public HoaDonDto thanhToan(Long donHangId, Long taiKhoanId, String phuongThuc,
            BigDecimal soTienKhachDua, String maGiaoDich) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("taiKhoanId", taiKhoanId);
        body.put("phuongThuc", phuongThuc);
        body.put("soTienKhachDua", soTienKhachDua);
        body.put("maGiaoDich", maGiaoDich);
        return guiPost("/api/don-hang/" + donHangId + "/thanh-toan", body, HoaDonDto.class);
    }

    /** Tìm khách hàng theo tên, số điện thoại hoặc mã. tuKhoa rỗng thì lấy 50 khách đầu tiên. */
    public List<KhachHangDto> timKhachHang(String tuKhoa) {
        String duongDan = "/api/khach-hang";
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            // Mã hóa từ khóa để tiếng Việt và ký tự đặc biệt đi qua URL an toàn
            duongDan += "?tuKhoa=" + URLEncoder.encode(tuKhoa.trim(), StandardCharsets.UTF_8);
        }
        return layDanhSach(duongDan, new TypeReference<List<KhachHangDto>>() { });
    }

    /** Lấy danh sách đơn. trangThai = null thì lấy 100 đơn mới nhất. */
    public List<DonHangTomTatDto> layDanhSachDon(String trangThai) {
        String duongDan = "/api/don-hang";
        if (trangThai != null && !trangThai.isBlank()) {
            duongDan += "?trangThai=" + trangThai; // mã trạng thái chỉ gồm chữ HOA và dấu gạch dưới
        }
        return layDanhSach(duongDan, new TypeReference<List<DonHangTomTatDto>>() { });
    }

    /** Đổi trạng thái đơn. lyDo chỉ bắt buộc khi trangThaiMoi là DA_HUY. */
    public DonHangDto doiTrangThai(Long donHangId, Long taiKhoanId, String trangThaiMoi, String lyDo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("taiKhoanId", taiKhoanId);
        body.put("trangThaiMoi", trangThaiMoi);
        body.put("lyDo", lyDo);
        return guiPut("/api/don-hang/" + donHangId + "/trang-thai", body, DonHangDto.class);
    }

    /** Gửi GET và đổi JSON thành danh sách đối tượng Java. */
    private <T> List<T> layDanhSach(String duongDan, TypeReference<List<T>> kieu) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + duongDan))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), kieu);
            }
            throw new ApiException("Lỗi máy chủ (mã " + response.statusCode() + ")");

        } catch (IOException e) {
            throw new ApiException("Không kết nối được máy chủ (backend chưa chạy?)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Yêu cầu bị gián đoạn");
        }
    }

    private <T> T guiPost(String duongDan, Object body, Class<T> kieu) {
        return guiJson("POST", duongDan, body, kieu);
    }

    private <T> T guiPut(String duongDan, Object body, Class<T> kieu) {
        return guiJson("PUT", duongDan, body, kieu);
    }

    /** Gửi yêu cầu có nội dung JSON (POST hoặc PUT) và đổi kết quả thành đối tượng Java. Mã 2xx là thành công. */
    private <T> T guiJson(String phuongThuc, String duongDan, Object body, Class<T> kieu) {
        try {
            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + duongDan))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .method(phuongThuc, HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return mapper.readValue(response.body(), kieu);
            }
            throw new ApiException(docThongBaoLoi(response));

        } catch (IOException e) {
            throw new ApiException("Không kết nối được máy chủ (backend chưa chạy?)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Yêu cầu bị gián đoạn");
        }
    }

    /** Lấy nội dung lỗi {"message": "..."} mà backend gửi về. */
    private String docThongBaoLoi(HttpResponse<String> response) {
        try {
            JsonNode node = mapper.readTree(response.body());
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (IOException e) {
            // Nội dung trả về không phải JSON, dùng thông báo mặc định bên dưới
        }
        return "Lỗi máy chủ (mã " + response.statusCode() + ")";
    }
}
