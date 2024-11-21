package tiameds.com.tiameds.dto.lab;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceDTO {

    private int id;
    private String name;
    private String description;
    private double price;
    private int duration;
    private double coverageLimit;  // Optional, if you decide to include it
    private String coverageType;  // Optional, if you decide to include it
    private String status;  // Optional, e.g., "Active", "Inactive"
    private String provider;  // Optional, if you decide to include it

}
