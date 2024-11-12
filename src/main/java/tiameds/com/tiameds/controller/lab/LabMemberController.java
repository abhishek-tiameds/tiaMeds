package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.UserInLabDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.services.lab.UserLabService;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lab/admin")
@Tag(name = "Lab Member Controller", description = "create, get, update and delete lab members")
public class LabMemberController {

    @Autowired
    private UserLabService userLabService;
    @Autowired
    private UserAuthService userAuthService;
    @Autowired
    private LabRepository labRepository;

    @PostMapping("/add-member/{labId}/member/{userId}")
    public ResponseEntity<?> addMemberToLab(
            @PathVariable Long labId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        // Check if the user is authenticated
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);

        // Check if the lab exists
        Lab lab = labRepository.findById(labId).orElse(null);
        if (lab == null)
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);

        // Check if the user exists (assuming you have a UserRepository or similar)
        User userToAdd = userLabService.getUserById(userId);
        if (userToAdd == null)
            return ApiResponseHelper.errorResponse("User to be added not found", HttpStatus.NOT_FOUND);

        //check createor of the lab
        if (!lab.getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to get members of this lab", HttpStatus.UNAUTHORIZED);
        }
        // Add the user to the lab's members
        if (lab.getMembers().contains(userToAdd)) {
            return ApiResponseHelper.errorResponse("User is already a member of this lab", HttpStatus.CONFLICT);
        }
        lab.getMembers().add(userToAdd);
        labRepository.save(lab);
        return ApiResponseHelper.successResponse("User added to lab successfully", HttpStatus.OK);
    }


    //get all members of a lab
    @GetMapping("/get-members/{labId}")
    public ResponseEntity<?> getLabMembers(@PathVariable Long labId, @RequestHeader("Authorization") String token) {
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null) {
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);
        }

        Lab lab = labRepository.findById(labId).orElse(null);
        if (lab == null) {
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        if (!lab.getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to view members of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Map the members to UserInLabDTO
        List<UserInLabDTO> memberDTOs = lab.getMembers().stream()
                .map(user -> new UserInLabDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()
                        ))).collect(Collectors.toList());

        return ResponseEntity.ok(memberDTOs);
    }


    //remove a member from a lab
    @DeleteMapping("/remove-member/{labId}/member/{userId}")
    public ResponseEntity<?> removeMemberFromLab(
            @PathVariable Long labId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);

        Lab lab = labRepository.findById(labId).orElse(null);
        if (lab == null)
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);

        User userToRemove = userLabService.getUserById(userId);
        if (userToRemove == null)
            return ApiResponseHelper.errorResponse("User to be removed not found", HttpStatus.NOT_FOUND);


        //check createor of the lab
        if (!lab.getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to remove members from this lab", HttpStatus.UNAUTHORIZED);
        }

        if (!lab.getMembers().contains(userToRemove)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.NOT_FOUND);
        }

        lab.getMembers().remove(userToRemove);
        labRepository.save(lab);
        return ApiResponseHelper.successResponse("User removed from lab successfully", HttpStatus.OK);
    }


}
