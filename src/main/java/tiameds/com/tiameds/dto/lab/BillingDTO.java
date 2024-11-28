package tiameds.com.tiameds.dto.lab;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillingDTO {
    private BigDecimal totalAmount;
    private String paymentStatus; // PAID, UNPAID, PARTIAL
    private String paymentMethod; // CASH, CARD, ONLINE
    private String paymentDate; // Date of payment
    private BigDecimal discount;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal netAmount;
}