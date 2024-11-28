package tiameds.com.tiameds.dto.lab;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VisitDTO {
    private LocalDate visitDate;
    private String visitType; // IN-PATIENT, OUT-PATIENT, EMERGENCY
    private String visitStatus; // ACTIVE, DISCHARGED, CANCELLED
    private String visitDescription;
    private Long doctorId;
    private List<Long> testIds; // Test IDs associated with the visit
    private List<Long> packageIds; // Health package IDs
    private List<Long> insuranceIds; // Insurance IDs
    private BillingDTO billing; // Billing details
}