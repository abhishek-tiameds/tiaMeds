package tiameds.com.tiameds.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;


@Data
@AllArgsConstructor
public class AuthResponse {
    private HttpStatus status;
    private String message;
    private String token;
    private LoginResponse data;
}
