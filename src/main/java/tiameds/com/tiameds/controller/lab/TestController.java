package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.TestDTO;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.Test;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.repository.TestRepository;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.List;
import java.util.Optional;
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

    // 1. Get all tests of a particular lab
    @GetMapping("/{labId}/tests")
    public ResponseEntity<?> getAllTests(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token) {
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

            // Retrieve and map all tests of the lab to DTOs
            List<TestDTO> testDTOs = testRepository.findByLabId(labId).stream().map(test -> new TestDTO(
                    test.getId(),
                    test.getCategory(),
                    test.getName(),
                    test.getPrice(),
                    test.getCreatedAt(),
                    test.getUpdatedAt(),
                    test.getLab().getId())).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseHelper.successResponse("Tests retrieved successfully", testDTOs).getBody());

        } catch (Exception e) {
            // Handle unexpected errors
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 2. Add a new test to a lab
    @PostMapping("/{labId}/add")
    public ResponseEntity<?> addTest(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token,
            @RequestBody TestDTO testDTO) {
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

            //check if the test already exists
            Optional<Test> testOptional = testRepository.findByCategoryAndName(testDTO.getCategory(), testDTO.getName());
            if (testOptional.isPresent()) {
                return ApiResponseHelper.errorResponse("Test already exists", HttpStatus.CONFLICT);
            }

            Lab lab = labOptional.get();

            //check you are the creator of the lab then you can delete the test
            if (!lab.getCreatedBy().equals(currentUser.orElse(null))) {
                // Comparison of actual objects
                System.out.println("lab.getCreatedBy() = " + lab.getCreatedBy() + " currentUser = " + currentUser);
                return ApiResponseHelper.errorResponse("You are not authorized to add tests to this lab", HttpStatus.UNAUTHORIZED);
            }


            // Create a new test entity
            Test test = new Test();
            test.setCategory(testDTO.getCategory());
            test.setName(testDTO.getName());
            test.setPrice(testDTO.getPrice());
            test.setLab(lab);

            // Save the test and map to DTO
            Test savedTest = testRepository.save(test);
            TestDTO savedTestDTO = new TestDTO(savedTest.getId(),
                    savedTest.getCategory(),
                    savedTest.getName(),
                    savedTest.getPrice(),
                    savedTest.getCreatedAt(),
                    savedTest.getUpdatedAt(),
                    savedTest.getLab().getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseHelper
                            .successResponse("Test added successfully", savedTestDTO).getBody());
        } catch (Exception e) {
            // Handle unexpected errors
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 3. Update an existing test
    @PutMapping("/{labId}/tests/{testId}/update")
    public ResponseEntity<?> updateTest(
            @PathVariable Long testId,
            @RequestHeader("Authorization") String token,
            @RequestBody TestDTO testDTO) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }
            // Check if the test exists
            Optional<Test> testOptional = testRepository.findById(testId);
            if (testOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Test not found", HttpStatus.NOT_FOUND);
            }

            Test test = testOptional.get();
            Lab lab = test.getLab();

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not authorized to update this test", HttpStatus.UNAUTHORIZED);
            }

            //check you are the creator of the lab then you can delete the test
            // Update test details
            test.setCategory(testDTO.getCategory());
            test.setName(testDTO.getName());
            test.setPrice(testDTO.getPrice());
            Test updatedTest = testRepository.save(test);
            TestDTO updatedTestDTO = new TestDTO(
                    updatedTest.getId(),
                    updatedTest.getCategory(),
                    updatedTest.getName(),
                    updatedTest.getPrice(),
                    updatedTest.getCreatedAt(),
                    updatedTest.getUpdatedAt(),
                    updatedTest.getLab().getId());
            return ResponseEntity.ok(ApiResponseHelper.successResponse("Test updated successfully", updatedTestDTO).getBody());
        } catch (Exception e) {
            // Handle unexpected errors
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Delete a test by ID
    @DeleteMapping("/{labId}/tests/{testId}/delete")
    public ResponseEntity<?> deleteTest(
            @PathVariable Long testId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }


            // Check if the test exists
            Optional<Test> testOptional = testRepository.findById(testId);
            if (testOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Test not found", HttpStatus.NOT_FOUND);
            }

            Test test = testOptional.get();
            Lab lab = test.getLab();

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not authorized to delete this test", HttpStatus.UNAUTHORIZED);
            }

            //check you are the creator of the lab then you can delete the test
            if (!lab.getCreatedBy().equals(currentUser)) {
                return ApiResponseHelper.errorResponse("You are not authorized to remove members from this lab", HttpStatus.UNAUTHORIZED);
            }

            testRepository.delete(test);
            return ResponseEntity.ok(ApiResponseHelper.successResponse("Test deleted successfully", HttpStatus.OK).getBody());
        } catch (Exception e) {
            // Handle unexpected errors
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 5. Get a test by ID
    @GetMapping("/{labId}/tests/{testId}")
    public ResponseEntity<?> getTestById(
            @PathVariable Long testId,
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate token format
            Optional<User> currentUser = userAuthService.authenticateUser(token);
            if (currentUser.isEmpty()) {
                return ApiResponseHelper.errorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }

            //check if the lab exists
            Optional<Lab> labOptional = labRepository.findById(labId);
            if (labOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
            }

            // Check if the test exists on lab
            Optional<Test> testOptional = testRepository.findById(testId);
            if (testOptional.isEmpty()) {
                return ApiResponseHelper.errorResponse("Test not found", HttpStatus.NOT_FOUND);
            }

            Test test = testOptional.get();
            Lab lab = test.getLab();

            // Check if the user is a member of the lab
            if (!currentUser.get().getLabs().contains(lab)) {
                return ApiResponseHelper.errorResponse("User is not authorized to view this test", HttpStatus.UNAUTHORIZED);
            }

            // Retrieve and map the test to DTO
            TestDTO testDTO = new TestDTO(
                    test.getId(),
                    test.getCategory(),
                    test.getName(),
                    test.getPrice(),
                    test.getCreatedAt(),
                    test.getUpdatedAt(),
                    test.getLab().getId());

            return ResponseEntity.ok(ApiResponseHelper.successResponse("Test retrieved successfully", testDTO).getBody());
        } catch (Exception e) {
            // Handle unexpected errors
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    // upload test csv file
    @PostMapping("/{labId}/tests/upload")
    public ResponseEntity<?> uploadTest(
            @PathVariable Long labId,
            @RequestHeader("Authorization") String token,
            @RequestBody TestDTO testDTO) {
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

            //check if the test already exists
            Optional<Test> testOptional = testRepository.findByCategoryAndName(testDTO.getCategory(), testDTO.getName());
            if (testOptional.isPresent()) {
                return ApiResponseHelper.errorResponse("Test already exists", HttpStatus.CONFLICT);
            }

            Lab lab = labOptional.get();

            //check you are the creator of the lab then you can delete the test
            if (!lab.getCreatedBy().equals(currentUser)) {
                return ApiResponseHelper.errorResponse("You are not authorized to remove members from this lab", HttpStatus.UNAUTHORIZED);
            }

            // Create a new test entity
            Test test = new Test();
            test.setCategory(testDTO.getCategory());
            test.setName(testDTO.getName());
            test.setPrice(testDTO.getPrice());
            test.setLab(lab);

            // Save the test and map to DTO
            Test savedTest = testRepository.save(test);
            TestDTO savedTestDTO = new TestDTO(savedTest.getId(),
                    savedTest.getCategory(),
                    savedTest.getName(),
                    savedTest.getPrice(),
                    savedTest.getCreatedAt(),
                    savedTest.getUpdatedAt(),
                    savedTest.getLab().getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseHelper
                            .successResponse("Test added successfully", savedTestDTO).getBody());
        } catch (Exception e) {
            // Handle unexpected errors
            return ApiResponseHelper.errorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

