package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.PatientDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.services.lab.BillingService;
import tiameds.com.tiameds.services.lab.PatientService;
import tiameds.com.tiameds.services.lab.VisitService;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.Optional;


@RestController
@RequestMapping("/lab")
@Tag(name = "Patient Controller", description = "Endpoints for managing patients in a lab")
public class PatientController {

    private final PatientService patientService;
    private final VisitService visitService;
    private final BillingService billingService;
    private final UserAuthService userAuthService;
    private final LabRepository labRepository;


    public PatientController(PatientService patientService, VisitService visitService, BillingService billingService, UserAuthService userAuthService, LabRepository labRepository) {
        this.patientService = patientService;
        this.visitService = visitService;
        this.billingService = billingService;
        this.userAuthService = userAuthService;
        this.labRepository = labRepository;
    }

    // Add your code here

    //add patient
    @PostMapping("/{labId}/add-patient")
    public ResponseEntity<?> addPatient(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token,
            @RequestBody PatientDTO patientDTO
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Check if the lab exists
            Optional<Lab> labOptional = labRepository.findById(labId);
            if (labOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
            }

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(labOptional.get())) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Check if the patient already exists by phone
            if (patientService.existsByPhone(patientDTO.getPhone())) {
                return ApiResponseHelper.errorResponse("Patient with this phone number already exists", HttpStatus.BAD_REQUEST);
            }

            // Add patient
            patientService.savePatientWithDetails(labOptional.get(), patientDTO);

            return ApiResponseHelper.successResponse("Patient added successfully", HttpStatus.CREATED);


        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    //get all patients
    @GetMapping("/{labId}/patients")
    public ResponseEntity<?> getAllPatients(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Check if the lab exists
            Optional<Lab> labOptional = labRepository.findById(labId);
            if (labOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
            }

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(labOptional.get())) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            return ResponseEntity.ok(patientService.getAllPatientsByLabId(labId));

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    //get patient by id of the lab
    @GetMapping("/{labId}/patient/{patientId}")
    public ResponseEntity<?> getPatientById(
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

            // Check if the lab exists
            Optional<Lab> labOptional = labRepository.findById(labId);
            if (labOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
            }

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(labOptional.get())) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            return ResponseEntity.ok(patientService.getPatientById(patientId, labId));

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    //update patient
    @PutMapping("/{labId}/update-patient/{patientId}")
    public ResponseEntity<?> updatePatient(
            @PathVariable Long labId,
            @PathVariable Long patientId,
            @RequestHeader("Authorization") String token,
            @RequestBody PatientDTO patientDTO
    ) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            // Check if the lab exists
            Optional<Lab> labOptional = labRepository.findById(labId);
            if (labOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
            }

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(labOptional.get())) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Check if the patient exists
            if (!patientService.existsById(patientId)) {
                return ApiResponseHelper.errorResponse("Patient not found", HttpStatus.NOT_FOUND);
            }

            // Update patient
            patientService.updatePatient(patientId, patientDTO);

            return ApiResponseHelper.successResponse("Patient updated successfully", HttpStatus.OK);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    //delete patient of the respective lab only
    @DeleteMapping("/{labId}/delete-patient/{patientId}")
    public ResponseEntity<?> deletePatient(
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

            // Check if the lab exists
            Optional<Lab> labOptional = labRepository.findById(labId);
            if (labOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
            }

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(labOptional.get())) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Check if the patient exists
            if (!patientService.existsById(patientId)) {
                return ApiResponseHelper.errorResponse("Patient not found", HttpStatus.NOT_FOUND);
            }

            // Delete patient
            patientService.deletePatient(patientId, labId);

            return ApiResponseHelper.successResponse("Patient deleted successfully", HttpStatus.OK);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



}
