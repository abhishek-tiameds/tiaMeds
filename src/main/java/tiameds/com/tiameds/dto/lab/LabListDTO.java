package tiameds.com.tiameds.dto.lab;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabListDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String description;
    private String createdByName;
}