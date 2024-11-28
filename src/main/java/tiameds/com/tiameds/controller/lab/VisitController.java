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


    // get list of patient visits of respective lab
    @GetMapping("/{labId}/visits")
    public ResponseEntity<?> getVisits(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Get the list of visits
            return ResponseEntity.ok(visitService.getVisits(labId, currentUser));

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // update the visit details
    @PutMapping("/{labId}/update-visit/{visitId}")
    public ResponseEntity<?> updateVisit(
            @PathVariable Long labId,
            @PathVariable Long visitId,
            @RequestBody VisitDTO visitDTO,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Update the visit
            visitService.updateVisit(labId, visitId, visitDTO, currentUser);

            return ApiResponseHelper.successResponse("Visit updated successfully", HttpStatus.OK);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // delete the visit
    @DeleteMapping("/{labId}/delete-visit/{visitId}")
    public ResponseEntity<?> deleteVisit(
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

            // Delete the visit
            visitService.deleteVisit(labId, visitId, currentUser);

            return ApiResponseHelper.successResponse("Visit deleted successfully", HttpStatus.OK);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // get the visit details by visit ID
    @GetMapping("/{labId}/visit/{visitId}")
    public ResponseEntity<?> getVisit(
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

            // Get the visit details
            return ResponseEntity.ok(visitService.getVisit(labId, visitId, currentUser));

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // get the visit details by patient ID
    @GetMapping("/{labId}/patient/{patientId}/visit")
    public ResponseEntity<?> getVisitByPatient(
            @PathVariable Long labId,
            @PathVariable Long patientId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Get the visit details
            return ResponseEntity.ok(visitService.getVisitByPatient(labId, patientId, currentUser));

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }








}
