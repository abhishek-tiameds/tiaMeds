package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.InsuranceDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.services.lab.InsuranceServices;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

@RestController
@RequestMapping("/lab/admin/insurance")
@Tag(name = "Insurance", description = "Endpoints for managing insurance admin can add insurance to a lab")
public class InsuranceController {

    private final InsuranceServices insuranceServices;
    private final UserAuthService userAuthService;
    private final LabRepository labRepository;


    public InsuranceController(InsuranceServices insuranceServices, UserAuthService userAuthService, LabRepository labRepository) {
        this.insuranceServices = insuranceServices;
        this.userAuthService = userAuthService;
        this.labRepository = labRepository;
    }


    // Add insurance
    @PostMapping("{labId}")
    public ResponseEntity<?> addInsurance(@PathVariable("labId") Long labId, @RequestBody InsuranceDTO insuranceDTO, @RequestHeader("Authorization") String token) {

        try {
            // Authenticate the user using the provided token
            User currentUser = userAuthService.authenticateUser(token).orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the lab exists in the repository
            Lab lab = labRepository.findById(labId).orElseThrow(() -> new RuntimeException("Lab not found"));

            // Verify if the current user is associated with the lab
            if (!currentUser.getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }
            // Delegate to the service layer
            insuranceServices.addInsurance(labId, insuranceDTO);

            return ApiResponseHelper.successResponse("Insurance added successfully", insuranceDTO);

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // get all insurance of a particular lab
    @GetMapping("{labId}")
    public ResponseEntity<?> getAllInsurance(@PathVariable("labId") Long labId, @RequestHeader("Authorization") String token) {
        try {
            // Authenticate the user using the provided token
            User currentUser = userAuthService.authenticateUser(token).orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the lab exists in the repository
            Lab lab = labRepository.findById(labId).orElseThrow(() -> new RuntimeException("Lab not found"));

            // Verify if the current user is associated with the lab
            if (!currentUser.getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Delegate to the service layer
            return ApiResponseHelper.successResponse("Insurance retrieved successfully", insuranceServices.getAllInsurance(labId));

        } catch (Exception e) {
            return ApiResponseHelper.errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }








}
