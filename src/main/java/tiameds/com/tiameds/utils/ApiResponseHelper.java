package tiameds.com.tiameds.utils;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseHelper {

    public static ResponseEntity<ApiResponse<Object>> successResponse(String message, Object data) {
        ApiResponse<Object> response = new ApiResponse<>("success", message, data);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<ApiResponse<String>> errorResponse(String message, HttpStatus status) {
        ApiResponse<String> response = new ApiResponse<>("error", message, null);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> successResponseWithData(T data) {
        ApiResponse<T> response = new ApiResponse<>("success", "Operation successful", data);
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> errorResponseWithData(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>("error", message, data);
        return ResponseEntity.ok(response);
    }
}
