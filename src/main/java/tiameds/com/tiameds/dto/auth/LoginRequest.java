package tiameds.com.tiameds.dto.auth;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String username;
    private String token;
    private List<String> roles; // To store user roles
}
