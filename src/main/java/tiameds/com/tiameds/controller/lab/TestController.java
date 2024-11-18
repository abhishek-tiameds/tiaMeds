package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.TestDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.Test;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.repository.TestRepository;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/lab")
@Tag(name = "Lab Tests", description = "Endpoints for managing lab tests")
public class TestController {

    private final LabRepository labRepository;
    private final TestRepository testRepository;
    private final UserAuthService userAuthService;

    public TestController(LabRepository labRepository, TestRepository testRepository, UserAuthService userAuthService) {
        this.labRepository = labRepository;
        this.testRepository = testRepository;
        this.userAuthService = userAuthService;
    }

    @GetMapping("/{labId}/tests")
    public ResponseEntity<?> getAllTests(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token) {
        try {
            // Authenticate the user using the provided token
            User currentUser = userAuthService.authenticateUser(token)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the lab exists in the repository
            Lab lab = labRepository.findById(labId)
                    .orElseThrow(() -> new RuntimeException("Lab not found"));

            // Verify if the current user is associated with the lab
            if (!currentUser.getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Retrieve, sort by ID in ascending order, and map all tests to DTOs
            List<TestDTO> testDTOs = lab.getTests().stream()
                    .sorted(Comparator.comparingLong(Test::getId)) // Sort by ID in ascending order
                    .map(test -> new TestDTO(
                            test.getId(),
                            test.getCategory(),
                            test.getName(),
                            test.getPrice(),
                            test.getCreatedAt(),
                            test.getUpdatedAt()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponseHelper.successResponse("Tests retrieved successfully", testDTOs).getBody());

        } catch (Exception e) {
            // Handle unexpected exceptions and provide meaningful error messages
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 2. Add a new test to a lab
    @PostMapping("/{labId}/add")
    public ResponseEntity<?> addTest(
            @PathVariable Long labId,
            @RequestBody TestDTO testDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Authenticate the user using the provided token
            User currentUser = userAuthService.authenticateUser(token)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the lab exists in the repository
            Lab lab = labRepository.findById(labId)
                    .orElseThrow(() -> new RuntimeException("Lab not found"));

            // Verify if the current user is associated with the lab
            if (!currentUser.getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }


            // check the test is already in the  the particular lab
            Set<Test> tests = lab.getTests();
            for (Test test : tests) {
                if (test.getName().equals(testDTO.getName())) {
                    return ApiResponseHelper.errorResponse("Test already exists in the lab", HttpStatus.BAD_REQUEST);
                }
            }

            // Create a new Test entity from the DTO
            Test test = new Test();
            test.setCategory(testDTO.getCategory());
            test.setName(testDTO.getName());
            test.setPrice(testDTO.getPrice());

            // Add the test to the lab and maintain the bidirectional relationship
            lab.addTest(test);

            // Persist the new Test entity and update the Lab entity
            labRepository.save(lab); // This will cascade and save the Test entity if properly configured

            // Optionally, map the saved Test back to a DTO to include generated data like ID
            TestDTO savedTestDTO = new TestDTO(
                    test.getId(),
                    test.getCategory(),
                    test.getName(),
                    test.getPrice(),
                    test.getCreatedAt(),
                    test.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponseHelper.successResponse("Test added successfully", savedTestDTO).getBody());

        } catch (Exception e) {
            // Handle unexpected exceptions and provide meaningful error messages
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 3. Remove a test from a lab
    @DeleteMapping("/{labId}/remove/{testId}")
    @Transactional
    public ResponseEntity<?> removeTest(
            @PathVariable Long labId,
            @PathVariable Long testId,
            @RequestHeader("Authorization") String token) {
        try {
            // Authenticate the user using the provided token
            User currentUser = userAuthService.authenticateUser(token)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the lab exists in the repository
            Lab lab = labRepository.findById(labId)
                    .orElseThrow(() -> new RuntimeException("Lab not found"));

            // Verify if the current user is associated with the lab
            if (!currentUser.getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Check if the test exists in the repository
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));

            // Remove the test from the lab and maintain the bidirectional relationship
            lab.removeTest(test);

            // Persist the updated Lab entity
            labRepository.save(lab);

            return ResponseEntity.ok(ApiResponseHelper.successResponse("Test removed successfully", null).getBody());

        } catch (Exception e) {
            // Handle unexpected exceptions and provide meaningful error messages
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 4. Update a test in a lab
    @PutMapping("/{labId}/update/{testId}")
    public ResponseEntity<?> updateTest(
            @PathVariable Long labId,
            @PathVariable Long testId,
            @RequestBody TestDTO testDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Authenticate the user using the provided token
            User currentUser = userAuthService.authenticateUser(token)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the lab exists in the repository
            Lab lab = labRepository.findById(labId)
                    .orElseThrow(() -> new RuntimeException("Lab not found"));

            // Verify if the current user is associated with the lab
            if (!currentUser.getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Check if the test exists in the repository
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));

            // Update the test entity with the new data
            test.setCategory(testDTO.getCategory());
            test.setName(testDTO.getName());
            test.setPrice(testDTO.getPrice());

            // Persist the updated Test entity
            testRepository.save(test);

            return ResponseEntity.ok(ApiResponseHelper.successResponse("Test updated successfully", null).getBody());

        } catch (Exception e) {
            // Handle unexpected exceptions and provide meaningful error messages
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 5 get the test by id
    @GetMapping("/{labId}/test/{testId}")
    public ResponseEntity<?> getTestById(
            @PathVariable Long labId,
            @PathVariable Long testId,
            @RequestHeader("Authorization") String token) {
        try {
            // Authenticate the user using the provided token
            User currentUser = userAuthService.authenticateUser(token)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the lab exists in the repository
            Lab lab = labRepository.findById(labId)
                    .orElseThrow(() -> new RuntimeException("Lab not found"));

            // Verify if the current user is associated with the lab
            if (!currentUser.getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
            }

            // Check if the test exists in the repository
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));

            // Map the test entity to a DTO
            TestDTO testDTO = new TestDTO(
                    test.getId(),
                    test.getCategory(),
                    test.getName(),
                    test.getPrice(),
                    test.getCreatedAt(),
                    test.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponseHelper.successResponse("Test retrieved successfully", testDTO).getBody());

        } catch (Exception e) {
            // Handle unexpected exceptions and provide meaningful error messages
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
