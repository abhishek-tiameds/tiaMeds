package tiameds.com.tiameds.dto.auth;

import lombok.Data;

import java.util.List;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
