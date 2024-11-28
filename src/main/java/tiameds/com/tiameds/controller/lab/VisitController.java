package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.VisitDTO;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.repository.PatientRepository;
import tiameds.com.tiameds.services.lab.BillingService;
import tiameds.com.tiameds.services.lab.PatientService;
import tiameds.com.tiameds.services.lab.VisitService;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.Optional;


@RestController
@RequestMapping("/lab")
@Tag(name = "Visit Controller", description = "mannage the patient visit in the lab")
public class VisitController {

    // Add your code here

    private final PatientService patientService;
    private final VisitService visitService;
    private final BillingService billingService;
    private final UserAuthService userAuthService;
    private final LabRepository labRepository;
    private PatientRepository patientRepository;


    public VisitController(PatientService patientService, VisitService visitService, BillingService billingService, UserAuthService userAuthService, LabRepository labRepository) {
        this.patientService = patientService;
        this.visitService = visitService;
        this.billingService = billingService;
        this.userAuthService = userAuthService;
        this.labRepository = labRepository;
    }


    // Add visit for an existing patient at the lab
    @PostMapping("/{labId}/add-visit/{patientId}")
    public ResponseEntity<?> addVisit(
            @PathVariable Long labId,
            @PathVariable Long patientId,
            @RequestBody VisitDTO visitDTO,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Create the visit (save to DB)
            visitService.addVisit(labId, patientId, visitDTO, currentUser);

            return ApiResponseHelper.successResponse("Visit added successfully", HttpStatus.OK);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // get List of all visits  of Lab
    @GetMapping("/{labId}/visits")
    public ResponseEntity<?> getAllVisits(@PathVariable Long labId, @RequestHeader("Authorization") String token) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Check if the lab exists
            if (!labRepository.existsById(labId)) {
                return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
            }

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(labRepository.findById(labId).get())) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Get all visits of the lab
            return ApiResponseHelper.successResponse(visitService.getAllVisits(labId), HttpStatus.OK);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
