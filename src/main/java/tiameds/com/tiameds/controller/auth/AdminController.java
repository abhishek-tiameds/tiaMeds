package tiameds.com.tiameds.controller.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.entity.Role;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.services.auth.UserService;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Controller", description = "Operations pertaining to admin management")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/dashboard")
    public String dashboard() {
        return "Welcome to the Admin Dashboard!";
    }


    @GetMapping
    public String profile() {
        return "Welcome to the Admin Profile!";
    }

    // get the list of all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> users() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    //get the list of all roles
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> roles() {
        List<Role> roles = userService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    //create a new role
    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role newRole = userService.saveRole(role);
        return ResponseEntity.ok(newRole);
    }

    //assign a role to a user
    @PostMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<?> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        try {
            User updatedUser = userService.assignRole(userId, Math.toIntExact(roleId));
            // If the role assignment is successful, return the updated user
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            // Handle the case where either the user or role is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User or Role not found.");
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while assigning the role.");
        }
    }


    //remove a role from a user
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<?> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        try {
            User updatedUser = userService.removeRole(userId, Math.toIntExact(roleId));
            // If the role removal is successful, return the updated user
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            // Handle the case where either the user or role is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User or Role not found.");
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while removing the role.");
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the user.");
        }
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        try {
            User user = userService.getUser(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching the user.");
        }
    }


}
