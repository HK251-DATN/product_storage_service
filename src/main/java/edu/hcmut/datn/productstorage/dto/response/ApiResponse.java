package edu.hcmut.datn.productstorage.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private ApiResponseType type; // ERROR, WARN, GOOD, SKIP_AS_GOOD
    private String code;
    private String message;
    private T detail;
    private LocalDateTime timestamp;

    private static <T> ApiResponse<T> buildResponse(ApiResponseType type, String code, String message, T detail) {
        return ApiResponse.<T>builder()
                .type(type)
                .code(code)
                .message(message)
                .detail(detail)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> SUCCESS(String code, String message, T detail) {
        return buildResponse(ApiResponseType.GOOD, code, message, detail);
    }

    public static <T> ApiResponse<T> SKIP_AS_GOOD(String code, String message, T detail) {
        return buildResponse(ApiResponseType.SKIP_AS_GOOD, code, message, detail);
    }

    public static <T> ApiResponse<T> WARN(String code, String message, T detail) {
        return buildResponse(ApiResponseType.WARN, code, message, detail);
    }

    public static <T> ApiResponse<T> ERROR(String code, String message, T detail) {
        return buildResponse(ApiResponseType.ERROR, code, message, detail);
    }

    public enum ApiResponseType {
        ERROR,
        WARN,
        GOOD,
        SKIP_AS_GOOD
    }
}
