package tiameds.com.tiameds.controller.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.auth.LoginRequest;
import tiameds.com.tiameds.dto.auth.LoginResponse;
import tiameds.com.tiameds.dto.auth.RegisterRequest;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.services.auth.UserDetailsServiceImpl;
import tiameds.com.tiameds.services.auth.UserService;
import tiameds.com.tiameds.utils.JwtUtil;
import tiameds.com.tiameds.entity.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/public")
@Tag(name = "User Controller", description = "Operations pertaining to user management")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtils;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder, JwtUtil jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }


    @GetMapping("/health-check")
    public String healthCheck() {
        return "Service is up and running";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {

        // check feild is empty or not
        if (registerRequest.getUsername().isEmpty() || registerRequest.getPassword().isEmpty() || registerRequest.getEmail().isEmpty() || registerRequest.getFirstName().isEmpty() || registerRequest.getLastName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please fill all the fields");
        }

        // check email is valid or not
        if (!registerRequest.getEmail().contains("@") || !registerRequest.getEmail().contains(".")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please enter a valid email");
        }

        // check password length
        if (registerRequest.getPassword().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 8 characters long");
        }

        // check username length
        if (registerRequest.getUsername().length() < 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username must be at least 4 characters long");
        }

        // check email is already exist or not
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        // check username is already exist or not
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
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
        user.setModules(registerRequest.getModules());
        user.setEnabled(true);

        // Save the user
        userService.saveUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        // Load user details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

        // Generate JWT token
        final String jwt = jwtUtils.generateToken(userDetails.getUsername());

        // Fetch user details from UserService to include roles
        Optional<User> userOptional = userService.findByUsername(loginRequest.getUsername());
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        User user = userOptional.get();
        // Convert roles to a list of strings
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());


        List<String> modules = new ArrayList<>(user.getModules());



        // Create the response
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwt);
        loginResponse.setUsername(user.getUsername());
        loginResponse.setEmail(user.getEmail());
        loginResponse.setFirstName(user.getFirstName());
        loginResponse.setLastName(user.getLastName());
        loginResponse.setRoles(roles); // Set roles in the response
        loginResponse.setPhone(user.getPhone());
        loginResponse.setAddress(user.getAddress());
        loginResponse.setCity(user.getCity());
        loginResponse.setState(user.getState());
        loginResponse.setZip(user.getZip());
        loginResponse.setCountry(user.getCountry());
        loginResponse.setVerified(user.isVerified());
        loginResponse.setEnabled(user.isEnabled());
        loginResponse.setModules(modules);

        // Return the response with token and user details including roles
        return ResponseEntity.ok(loginResponse);
    }

}

