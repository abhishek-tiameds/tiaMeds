package tiameds.com.tiameds.controller.lab;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.services.lab.BillingService;
import tiameds.com.tiameds.services.lab.PatientService;
import tiameds.com.tiameds.services.lab.VisitService;
import tiameds.com.tiameds.utils.ApiResponse;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.Optional;

@RestController
@RequestMapping("/lab")
@Tag(name = "Billing Controller", description = "manage the billing in the lab")
public class BillingController {

    private final PatientService patientService;
    private final VisitService visitService;
    private final BillingService billingService;
    private final UserAuthService userAuthService;

    public BillingController(PatientService patientService, VisitService visitService, BillingService billingService, UserAuthService userAuthService) {
        this.patientService = patientService;
        this.visitService = visitService;
        this.billingService = billingService;
        this.userAuthService = userAuthService;
    }


    // update the billing of a visit in respected lab only
    @PutMapping("/{labId}/update-billing-status/{visitId}")
    public ResponseEntity<?> updateBillingStatus(
            @PathVariable Long labId,
            @PathVariable Long visitId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Update the billing status
            billingService.updateBillingStatus(labId, visitId, currentUser);

            return ApiResponseHelper.successResponse("Billing status updated successfully", HttpStatus.OK);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
