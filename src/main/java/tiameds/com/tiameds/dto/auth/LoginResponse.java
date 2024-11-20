package tiameds.com.tiameds.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tiameds.com.tiameds.dto.lab.ModuleDTO;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    @JsonProperty("is_verified")
    private boolean isVerified;
    private List<ModuleDTO> modules;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private boolean enabled;

}
