//package tiameds.com.tiameds.controller.auth;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import tiameds.com.tiameds.entity.User;
//import tiameds.com.tiameds.services.auth.UserService;
//
//@RestController
//@RequestMapping("/public")
//public class AuthController {
//
//    @Autowired
//    private UserService userService;
//
//    // User signup endpoint - Using POST instead of GET
//    @PostMapping("/signup")
//    public ResponseEntity<?> signup(@RequestBody User user) {
//        try {
//            User savedUser = userService.saveUser(user);
//            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
//        } catch (Exception e) {
//            // Handle exceptions such as duplicate users, etc.
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error during signup: " + e.getMessage());
//        }
//    }
//
//}





package tiameds.com.tiameds.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.services.auth.UserService;

@RestController
@RequestMapping("/public")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    // User signup endpoint - Using POST instead of GET
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            // Handle exceptions such as duplicate users, etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error during signup: " + e.getMessage());
        }
    }

    // User login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        try {
            String credential; // This will be either username or email

            if (loginUser.getUsername() != null) {
                credential = loginUser.getUsername();
            } else if (loginUser.getEmail() != null) {
                credential = loginUser.getEmail();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login failed: Missing username or email");
            }

            String password = loginUser.getPassword();

            // Check if the credential is an email or username
            User user;
            if (credential.contains("@")) {
                // If it's an email, fetch user by email
                user = userService.findByEmail(credential);
            } else {
                // Otherwise, fetch user by username
                user = userService.findByUsername(credential);
            }

            // Check if user exists and is not null
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: User not found");
            }

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), password)
            );

            // If authentication is successful, return the complete user details
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
        }
    }




}
