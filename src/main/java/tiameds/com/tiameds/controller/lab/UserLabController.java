package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.LabRequestDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.services.lab.UserLabService;
import tiameds.com.tiameds.utils.ApiResponse;
import tiameds.com.tiameds.utils.JwtUtil;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/lab/admin")
@Tag(name = "Lab Admin", description = "Endpoints for Lab Admin")
public class UserLabController {

    private final UserLabService userService;
    private final LabRepository labRepository;
    private final JwtUtil jwtUtils;


    public UserLabController(UserLabService userService, LabRepository labRepository, JwtUtil jwtUtils) {
        this.userService = userService;
        this.labRepository = labRepository;
        this.jwtUtils = jwtUtils;
    }

    //================================== List of lab API endpoints ===================================

    // 1. Add Lab
    // 2 Get Labs created by user
    // 2. Update Lab
    // 3. Delete Lab

    // to create a lab to particular user
    @PostMapping("/add-lab")
    public ResponseEntity<String> addLab(@RequestBody LabRequestDTO labRequestDTO, @RequestHeader("Authorization") String token) {
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        String currentUsername = jwtUtils.extractUsername(token.substring(7));
        Optional<User> currentUserOptional = userService.findByUsername(currentUsername);

        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        User currentUser = currentUserOptional.get();

        //check if the lab already exists
        if (userService.existsLabByName(labRequestDTO.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lab already exists");
        }

        Lab lab = new Lab();
        lab.setName(labRequestDTO.getName());
        lab.setAddress(labRequestDTO.getAddress());
        lab.setCity(labRequestDTO.getCity());
        lab.setState(labRequestDTO.getState());
        lab.setDescription(labRequestDTO.getDescription());
        lab.setCreatedBy(currentUser);
        labRepository.save(lab);
        return ResponseEntity.status(HttpStatus.CREATED).body("Lab added successfully" + lab);
    }

    @GetMapping("/get-labs")
    public ResponseEntity<?> getLabsCreatedByUser(@RequestHeader("Authorization") String token) {
        // Validate token format
        if (!token.startsWith("Bearer ")) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "Invalid token",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Extract the username from the token
        String currentUsername = jwtUtils.extractUsername(token.substring(7));

        // Fetch user details using the username
        Optional<User> currentUserOptional = userService.findByUsername(currentUsername);

        // If user is not found, return unauthorized response
        if (currentUserOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "User not found",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        User currentUser = currentUserOptional.get();

        // Fetch labs created by the user
        List<Lab> labs = labRepository.findByCreatedBy(currentUser);

        // If no labs are found, return a not found response
        if (labs.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "No labs found for this user",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // Return the list of labs created by the user
        ApiResponse<List<Lab>> successResponse = new ApiResponse<>(
                "success",
                "Labs found",
                labs
        );
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    // delete lab by their respective id
    @DeleteMapping("/delete-lab/{labId}")
    public ResponseEntity<?> deleteLab(@PathVariable Long labId, @RequestHeader("Authorization") String token) {
        // Validate token format
        if (!token.startsWith("Bearer ")) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "Invalid token",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Extract the username from the token
        String currentUsername = jwtUtils.extractUsername(token.substring(7));

        // Fetch user details using the username
        Optional<User> currentUserOptional = userService.findByUsername(currentUsername);

        // If user is not found, return unauthorized response
        if (currentUserOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "User not found",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        User currentUser = currentUserOptional.get();

        // Fetch the lab to be deleted
        Optional<Lab> labOptional = labRepository.findById(labId);

        // If lab is not found, return a not found response
        if (labOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "Lab not found",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Lab lab = labOptional.get();

        // Check if the lab is created by the current user
        if (!lab.getCreatedBy().equals(currentUser)) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "You are not authorized to delete this lab",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Delete the lab
        labRepository.delete(lab);

        // Return success response
        ApiResponse<String> successResponse = new ApiResponse<>(
                "success",
                "Lab deleted successfully",
                null
        );
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    // update lab by their respective id
    @PutMapping("/update-lab/{labId}")
    public ResponseEntity<?> updateLab(@PathVariable Long labId, @RequestBody LabRequestDTO labRequestDTO, @RequestHeader("Authorization") String token) {
        // Validate token format
        if (!token.startsWith("Bearer ")) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "Invalid token",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Extract the username from the token
        String currentUsername = jwtUtils.extractUsername(token.substring(7));

        // Fetch user details using the username
        Optional<User> currentUserOptional = userService.findByUsername(currentUsername);

        // If user is not found, return unauthorized response
        if (currentUserOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "User not found",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        User currentUser = currentUserOptional.get();

        // Fetch the lab to be updated
        Optional<Lab> labOptional = labRepository.findById(labId);

        // If lab is not found, return a not found response
        if (labOptional.isEmpty()) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "Lab not found",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Lab lab = labOptional.get();

        // Check if the lab is created by the current user
        if (!lab.getCreatedBy().equals(currentUser)) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    "error",
                    "You are not authorized to update this lab",
                    null
            );
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
        ApiResponse<String> successResponse = new ApiResponse<>(
                "success",
                "Lab updated successfully",
                null
        );
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }


    //================================== List of lab API endpoints ===================================



}

