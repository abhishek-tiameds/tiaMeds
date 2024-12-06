package tiameds.com.tiameds.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tiameds.com.tiameds.dto.lab.LabResponseDTO;

import java.util.HashMap;
import java.util.Map;


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

    public static <T> ResponseEntity<Map<String, Object>> successResponseWithDataAndMessage(String message, HttpStatus status, T data) {
        // Create a Map to hold the response structure
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);

        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<Map<String, Object>> errorResponseWithMessage(String message, HttpStatus status) {
        // Create a Map to hold the response structure
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);

        return new ResponseEntity<>(response, status);
    }


}
