package tiameds.com.tiameds.dto.lab;

import lombok.Data;

@Data
public class LabRequestDTO {
    private String name;
    private String address;
    private String city;
    private String state;
    private String description;
}
