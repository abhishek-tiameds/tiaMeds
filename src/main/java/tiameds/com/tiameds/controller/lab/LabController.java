package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.LabListDTO;
import tiameds.com.tiameds.dto.lab.LabRequestDTO;
import tiameds.com.tiameds.dto.lab.LabResponseDTO;
import tiameds.com.tiameds.dto.lab.UserResponseDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.services.lab.UserLabService;
import tiameds.com.tiameds.utils.ApiResponse;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.LabAccessableFilter;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/lab/admin")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Lab Admin", description = "Endpoints for Lab Admin")
public class LabController {

    private final UserLabService userService;
    private final LabRepository labRepository;
    private final UserAuthService userAuthService;
    private final LabAccessableFilter labAccessableFilter;


    public LabController(UserLabService userService, LabRepository labRepository, UserAuthService userAuthService, LabAccessableFilter labAccessableFilter) {
        this.userService = userService;
        this.labRepository = labRepository;
        this.userAuthService = userAuthService;
        this.labAccessableFilter = labAccessableFilter;
    }

    // create a new lab
    @PostMapping("/add-lab")
    public ResponseEntity<Map<String, Object>> addLab(
            @RequestBody LabRequestDTO labRequestDTO,
            @RequestHeader("Authorization") String token) {

        // Validate token format
        Optional<User> currentUserOptional = userAuthService.authenticateUser(token);
        if (currentUserOptional.isEmpty()) {
            ApiResponse<String> response = new ApiResponse<>("error", "User not found", null);
            return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
        }

        User currentUser = currentUserOptional.get();

        // Check if the lab already exists
        if (userService.existsLabByName(labRequestDTO.getName())) {
            ApiResponse<String> response = new ApiResponse<>("error", "Lab already exists", null);
            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        // Create and save the new lab
        Lab lab = new Lab();
        lab.setName(labRequestDTO.getName());
        lab.setAddress(labRequestDTO.getAddress());
        lab.setCity(labRequestDTO.getCity());
        lab.setState(labRequestDTO.getState());
        lab.setDescription(labRequestDTO.getDescription());
        lab.setIsActive(true);
        lab.setCreatedBy(currentUser);
        labRepository.save(lab);

        // Create DTOs for response
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getFirstName(),
                currentUser.getLastName()
        );

        LabResponseDTO labResponseDTO = new LabResponseDTO(
                lab.getId(),
                lab.getName(),
                lab.getAddress(),
                lab.getCity(),
                lab.getState(),
                lab.getDescription(),
                userResponseDTO
        );

        // Return success response
        return ApiResponseHelper.successResponseWithDataAndMessage("Lab created successfully", HttpStatus.OK, labResponseDTO);

    }

    // get all labs created by user
    @GetMapping("/get-labs")
    public ResponseEntity<?> getLabsCreatedByUser(
            @RequestHeader("Authorization") String token) {

        // Validate token format
        Optional<User> currentUserOptional = userAuthService.authenticateUser(token);

        // If user is not found, return unauthorized response
        if (currentUserOptional.isEmpty()) {
            return ApiResponseHelper.successResponseWithDataAndMessage("User not found", HttpStatus.UNAUTHORIZED, null);
        }

        User currentUser = currentUserOptional.get();
        // Fetch labs created by the user
        List<Lab> labs = labRepository.findByCreatedBy(currentUser);

        // If no labs are found, return a not found response
        if (labs.isEmpty()) {
            return ApiResponseHelper.successResponseWithDataAndMessage("No labs found", HttpStatus.OK, null);
        }


        // Map labs to LabListDTO
        List<LabListDTO> labListDTOs = labs.stream()
                .map(lab -> new LabListDTO(
                        lab.getId(),
                        lab.getName(),
                        lab.getAddress(),
                        lab.getCity(),
                        lab.getState(),
                        lab.getIsActive(),
                        lab.getDescription(),
                        lab.getCreatedBy().getUsername()
                ))
                .toList();

        return ApiResponseHelper.successResponseWithDataAndMessage("Labs fetched successfully", HttpStatus.OK, labListDTOs);

    }


    // delete lab by their respective id
    @DeleteMapping("/delete-lab/{labId}")
    public ResponseEntity<?> deleteLab(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token) {

        // Validate token format
        Optional<User> currentUserOptional = userAuthService.authenticateUser(token);
        // If user is not found, return unauthorized response
        if (currentUserOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>("error", "User not found", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        User currentUser = currentUserOptional.get();
        // Fetch the lab to be deleted
        Optional<Lab> labOptional = labRepository.findById(labId);

        // If lab is not found, return a not found response
        if (labOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>("error", "Lab not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Lab lab = labOptional.get();

        // Check if the lab is created by the current user
        if (!lab.getCreatedBy().equals(currentUser)) {
            ApiResponse<String> errorResponse = new ApiResponse<>("error", "You are not authorized to delete this lab", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Delete the lab
        labRepository.delete(lab);

        // Return success response
        return ApiResponseHelper.successResponseWithDataAndMessage("Lab deleted successfully", HttpStatus.OK, null);
    }

    // update lab by their respective id
    @PutMapping("/update-lab/{labId}")
    public ResponseEntity<?> updateLab(
            @PathVariable Long labId,
            @RequestBody LabRequestDTO labRequestDTO,
            @RequestHeader("Authorization") String token) {
        // Validate token format
        Optional<User> currentUserOptional = userAuthService.authenticateUser(token);
        // If user is not found, return unauthorized response
        if (currentUserOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>("error", "User not found", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        User currentUser = currentUserOptional.get();

        // Fetch the lab to be updated
        Optional<Lab> labOptional = labRepository.findById(labId);

        // If lab is not found, return a not found response
        if (labOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>("error", "Lab not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Lab lab = labOptional.get();

        // Check if the lab is created by the current user
        if (!lab.getCreatedBy().equals(currentUser)) {
            ApiResponse<String> errorResponse = new ApiResponse<>("error", "You are not authorized to update this lab", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Update the lab details
        lab.setName(labRequestDTO.getName());
        lab.setAddress(labRequestDTO.getAddress());
        lab.setCity(labRequestDTO.getCity());
        lab.setState(labRequestDTO.getState());
        lab.setDescription(labRequestDTO.getDescription());
        labRepository.save(lab);
        // Return success response

        return ApiResponseHelper.successResponseWithDataAndMessage("Lab updated successfully", HttpStatus.OK, lab);
    }

}


