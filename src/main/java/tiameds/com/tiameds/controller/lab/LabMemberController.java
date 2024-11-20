package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.auth.RegisterRequest;
import tiameds.com.tiameds.dto.lab.UserInLabDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.services.auth.UserService;
import tiameds.com.tiameds.services.lab.UserLabService;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lab/admin")
@Tag(name = "Lab Member Controller", description = "create, get, update and delete lab members")
public class LabMemberController {

    private UserLabService userLabService;
    private UserAuthService userAuthService;
    private LabRepository labRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LabMemberController(
            UserLabService userLabService,
            UserAuthService userAuthService,
            LabRepository labRepository, UserService userService,
            PasswordEncoder passwordEncoder) {
        this.userLabService = userLabService;
        this.userAuthService = userAuthService;
        this.labRepository = labRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

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
    public ResponseEntity<?> getLabMembers(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token) {

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

        return ApiResponseHelper.successResponse("Lab members retrieved successfully", memberDTOs);
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


    //create a user in lab
    @PostMapping("/create-user/{labId}")
    public ResponseEntity<?> createUserInLab(
            @RequestBody RegisterRequest registerRequest,
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token) {


        // Check if the user is authenticated
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);


        // Check if the lab exists
        Lab lab = labRepository.findById(labId).orElse(null);
        if (lab == null)
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);


        //check createor of the lab
        if (!lab.getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to create user in this lab", HttpStatus.UNAUTHORIZED);
        }

        // check feild is empty or not
        if (registerRequest.getUsername().isEmpty() || registerRequest.getPassword().isEmpty() || registerRequest.getEmail().isEmpty() || registerRequest.getFirstName().isEmpty() || registerRequest.getLastName().isEmpty()) {
            return ApiResponseHelper.errorResponse("Please fill all the fields", HttpStatus.BAD_REQUEST);
        }

        // check email is valid or not
        if (!registerRequest.getEmail().contains("@") || !registerRequest.getEmail().contains(".")) {
            return ApiResponseHelper.errorResponse("Please enter a valid email", HttpStatus.BAD_REQUEST);
        }

        // check password length
        if (registerRequest.getPassword().length() < 8) {
            return ApiResponseHelper.errorResponse("Password must be at least 8 characters long", HttpStatus.BAD_REQUEST);
        }

        // check username length
        if (registerRequest.getUsername().length() < 4) {
            return ApiResponseHelper.errorResponse("Username must be at least 4 characters long", HttpStatus.BAD_REQUEST);
        }

        // check email is already exist or not
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ApiResponseHelper.errorResponse("Email already exists", HttpStatus.BAD_REQUEST);
        }

        // check username is already exist or not
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ApiResponseHelper.errorResponse("Username already exists", HttpStatus.BAD_REQUEST);
        }

        // Create a new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setAddress(registerRequest.getAddress());
        user.setCity(registerRequest.getCity());
        user.setState(registerRequest.getState());
        user.setZip(registerRequest.getZip());
        user.setCountry(registerRequest.getCountry());
        user.setVerified(registerRequest.isVerified());
//        user.setModules(registerRequest.getModules());
        user.setEnabled(true);
        user.setCreatedBy(currentUser);

        // Save the user
        userService.saveUser(user);

        // Add the user to the lab's members
        if (lab.getMembers().contains(user)) {
            return ApiResponseHelper.errorResponse("User is already a member of this lab", HttpStatus.CONFLICT);
        }
        lab.getMembers().add(user);
        labRepository.save(lab);
        return ApiResponseHelper.successResponse("User created and added to lab successfully", HttpStatus.OK);
    }


    //update member details in lab
    @PutMapping("/update-user/{userId}")
    public ResponseEntity<?> updateUserInLab(
            @RequestBody RegisterRequest registerRequest,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        // Check if the user is authenticated
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);

        // Check if the user exists
        User userToUpdate = userLabService.getUserById(userId);
        if (userToUpdate == null)
            return ApiResponseHelper.errorResponse("User not found", HttpStatus.NOT_FOUND);

        //check createor of the lab
        if (!userToUpdate.getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to update this user", HttpStatus.UNAUTHORIZED);
        }

        // check feild is empty or not
        if (registerRequest.getUsername().isEmpty() || registerRequest.getPassword().isEmpty() || registerRequest.getEmail().isEmpty() || registerRequest.getFirstName().isEmpty() || registerRequest.getLastName().isEmpty()) {
            return ApiResponseHelper.errorResponse("Please fill all the fields", HttpStatus.BAD_REQUEST);
        }

        // check email is valid or not
        if (!registerRequest.getEmail().contains("@") || !registerRequest.getEmail().contains(".")) {
            return ApiResponseHelper.errorResponse("Please enter a valid email", HttpStatus.BAD_REQUEST);
        }

        // check password length
        if (registerRequest.getPassword().length() < 8) {
            return ApiResponseHelper.errorResponse("Password must be at least 8 characters long", HttpStatus.BAD_REQUEST);
        }

        // check username length
        if (registerRequest.getUsername().length() < 4) {
            return ApiResponseHelper.errorResponse("Username must be at least 4 characters long", HttpStatus.BAD_REQUEST);
        }

        // Update the user
        userToUpdate.setUsername(registerRequest.getUsername());
        userToUpdate.setPassword(registerRequest.getPassword());
        userToUpdate.setEmail(registerRequest.getEmail());
        userToUpdate.setFirstName(registerRequest.getFirstName());
        userToUpdate.setLastName(registerRequest.getLastName());
        userToUpdate.setPhone(registerRequest.getPhone());
        userToUpdate.setAddress(registerRequest.getAddress());
        userToUpdate.setCity(registerRequest.getCity());
        userToUpdate.setState(registerRequest.getState());
        userToUpdate.setZip(registerRequest.getZip());
        userToUpdate.setCountry(registerRequest.getCountry());
        userToUpdate.setVerified(registerRequest.isVerified());
//        userToUpdate.setModules(registerRequest.getModules());
        userToUpdate.setEnabled(true);
        userToUpdate.setCreatedBy(currentUser);
        // Save the user
        userService.saveUser(userToUpdate);
        return ApiResponseHelper.successResponse("User updated successfully", HttpStatus.OK);
    }


    //delete user in lab if you are the creator and in user table there is field that contain a creator id
    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<?> deleteUserInLab(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        // Check if the user is authenticated
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);

        // Check if the user exists
        User userToDelete = userLabService.getUserById(userId);
        if (userToDelete == null)
            return ApiResponseHelper.errorResponse("User not found", HttpStatus.NOT_FOUND);

        //check createor of the lab
        if (!userToDelete.getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to delete this user", HttpStatus.UNAUTHORIZED);
        }

        // 1st remove members from lab
        // 2nd delete user

        // Remove the user from all labs
        List<Lab> labs = labRepository.findAll();
        for (Lab lab : labs) {
            if (lab.getMembers().contains(userToDelete)) {
                lab.getMembers().remove(userToDelete);
                labRepository.save(lab);
            }
        }

        // Delete the user
        userService.deleteUser(userToDelete.getId());

        return ApiResponseHelper.successResponse("User deleted successfully", HttpStatus.OK);

    }


    //========================= role assign and remove ========================

    //assign role to member
    @PutMapping("/assign-role/{userId}/role/{roleId}")
    public ResponseEntity<?> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @RequestHeader("Authorization") String token
    ) {

        // Check if the user is authenticated
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);

        // Check if the user exists
        User user = userLabService.getUserById(userId);
        if (user == null)
            return ApiResponseHelper.errorResponse("User not found", HttpStatus.NOT_FOUND);


        //check user is member of the lab or not
        Optional<Lab> lab = labRepository.findByMembers(user);
        if (lab.isEmpty()) {
            return ApiResponseHelper.errorResponse("User is not a member of any lab", HttpStatus.NOT_FOUND);
        }

        //check createor of the lab
        if (!lab.get().getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to assign role to this user", HttpStatus.UNAUTHORIZED);
        }

        //assign role to user
        try {
            User updatedUser = userService.assignRole(userId, Math.toIntExact(roleId));
            return ApiResponseHelper.successResponse("Role assigned successfully", updatedUser);
        } catch (EntityNotFoundException e) {
            return ApiResponseHelper.errorResponse("User or Role not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ApiResponseHelper.errorResponse("An error occurred while assigning the role", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //remove role from member
    @DeleteMapping("/remove-role/{userId}/role/{roleId}")
    public ResponseEntity<?> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @RequestHeader("Authorization") String token
    ) {

        // Check if the user is authenticated
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);

        // Check if the user exists
        User user = userLabService.getUserById(userId);
        if (user == null)
            return ApiResponseHelper.errorResponse("User not found", HttpStatus.NOT_FOUND);

        //check user is member of the lab or not
        Optional<Lab> lab = labRepository.findByMembers(user);
        if (lab.isEmpty()) {
            return ApiResponseHelper.errorResponse("User is not a member of any lab", HttpStatus.NOT_FOUND);
        }

        //check createor of the lab
        if (!lab.get().getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to remove role from this user", HttpStatus.UNAUTHORIZED);
        }

        //remove role from user
        try {
            User updatedUser = userService.removeRole(userId, Math.toIntExact(roleId));
            return ApiResponseHelper.successResponse("Role removed successfully", updatedUser);
        } catch (EntityNotFoundException e) {
            return ApiResponseHelper.errorResponse("User or Role not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ApiResponseHelper.errorResponse("An error occurred while removing the role", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //get user by id
    @GetMapping("/get-user/{userId}")
    public ResponseEntity<?> getUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token
    ) {

        // Check if the user is authenticated
        User currentUser = userAuthService.authenticateUser(token).orElse(null);
        if (currentUser == null)
            return ApiResponseHelper.errorResponse("User not found or unauthorized", HttpStatus.UNAUTHORIZED);

        // Check if the user exists
        User user = userLabService.getUserById(userId);
        if (user == null)
            return ApiResponseHelper.errorResponse("User not found", HttpStatus.NOT_FOUND);

        //check user is member of the lab or not
        Optional<Lab> lab = labRepository.findByMembers(user);
        if (lab.isEmpty()) {
            return ApiResponseHelper.errorResponse("User is not a member of any lab", HttpStatus.NOT_FOUND);
        }

        //check createor of the lab
        if (!lab.get().getCreatedBy().equals(currentUser)) {
            return ApiResponseHelper.errorResponse("You are not authorized to get this user", HttpStatus.UNAUTHORIZED);
        }

        return ApiResponseHelper.successResponse("User retrieved successfully", user);
    }


}
