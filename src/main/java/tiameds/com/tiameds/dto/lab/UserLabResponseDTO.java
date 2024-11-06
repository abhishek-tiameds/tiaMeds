package tiameds.com.tiameds.dto.lab;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class UserLabResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> modules;
    private List<String> roles;
    private String createdByUsername;
    private String labName;

}
