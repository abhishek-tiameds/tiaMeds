package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.HealthPackageRequest;
import tiameds.com.tiameds.entity.HealthPackage;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.Test;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.HealthPackageRepository;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.repository.TestRepository;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/admin/lab")
@Tag(name = "Package", description = "Package API which is used to manage packages")
public class HealthPackageController {

    private final LabRepository labRepository;
    private final TestRepository testRepository;
    private final UserAuthService userAuthService;
    private final HealthPackageRepository healthPackageRepository;


    //default constructor
    public HealthPackageController(LabRepository labRepository, TestRepository testRepository, UserAuthService userAuthService, HealthPackageRepository healthPackageRepository) {
        this.labRepository = labRepository;
        this.testRepository = testRepository;
        this.userAuthService = userAuthService;
        this.healthPackageRepository = healthPackageRepository;
    }


    @GetMapping("{labId}/packages")
    public ResponseEntity<?> getHealthPackages(
            @PathVariable("labId") Long labId,
            @RequestHeader("Authorization") String token
    ) {
        // Authenticate the user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the lab and check if it exists
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Fetch the health packages associated with the lab, including tests
        var packages = healthPackageRepository.findByLabs_Id(labId);

        if (packages.isEmpty()) {
            return ApiResponseHelper.errorResponse("No health packages found for this lab", HttpStatus.NOT_FOUND);
        }


        // Return the success response with the fetched packages
        return ApiResponseHelper.successResponse(
                "Packages fetched successfully",
                packages
        );
    }


    //create package
    @PostMapping("{labId}/package")
    public ResponseEntity<?> createHealthPackage(
            @PathVariable("labId") Long labId,
            @RequestBody HealthPackageRequest packageRequest, // Assuming a DTO is used to accept the data
            @RequestHeader("Authorization") String token
    ) {
        // Authenticate the user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the lab and check if it exists
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Fetch the health tests based on the provided test IDs
        List<Test> tests = testRepository.findAllById(packageRequest.getTestIds());  // Fetching tests by their IDs

        // Check if all test IDs exist in the database
        if (tests.size() != packageRequest.getTestIds().size()) {
            return ApiResponseHelper.errorResponse("One or more test IDs do not exist", HttpStatus.BAD_REQUEST);
        }


        // Create a new health package
        HealthPackage healthPackage = new HealthPackage();

        //check the lab have already package with same name
        Lab lab1 = labRepository.findById(labId).get();
        if (lab1.getHealthPackages().stream().anyMatch(healthPackage1 -> healthPackage1.getPackageName().equals(packageRequest.getPackageName()))) {
            return ApiResponseHelper.errorResponse("Package with the same name already exists", HttpStatus.BAD_REQUEST);
        }

        healthPackage.setPackageName(packageRequest.getPackageName());
        healthPackage.setPrice(packageRequest.getPrice());
        healthPackage.setTests(new HashSet<>(tests));  // Convert List to Set before adding tests

        // Associate the health package with the lab
        lab.getHealthPackages().add(healthPackage);

        // Save the health package to the database
        healthPackageRepository.save(healthPackage);

        // Return the success response with the created health package
        return ApiResponseHelper.successResponse(
                "Health package created successfully",
                healthPackage
        );
    }


    //get package by id
    @GetMapping("{labId}/package/{packageId}")
    public ResponseEntity<?> getHealthPackage(
            @PathVariable("labId") Long labId,
            @PathVariable("packageId") Long packageId,
            @RequestHeader("Authorization") String token
    ) {
        // Authenticate the user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the lab and check if it exists
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Fetch the health package based on the provided package ID
        var healthPackageOptional = healthPackageRepository.findById(packageId);

        // Check if the health package exists
        if (healthPackageOptional.isEmpty()) {
            return ApiResponseHelper.errorResponse("Health package not found", HttpStatus.NOT_FOUND);
        }

        HealthPackage healthPackage = healthPackageOptional.get();

        // Check if the health package is associated with the lab
        if (!healthPackage.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("Health package not found", HttpStatus.NOT_FOUND);
        }

        // Return the success response with the fetched health package
        return ApiResponseHelper.successResponse(
                "Health package fetched successfully",
                healthPackage
        );
    }


    //update package
    @PutMapping("{labId}/package/{packageId}")
    public ResponseEntity<?> updateHealthPackage(
            @PathVariable("labId") Long labId,
            @PathVariable("packageId") Long packageId,
            @RequestBody HealthPackageRequest packageRequest, // Assuming a DTO is used to accept the data
            @RequestHeader("Authorization") String token
    ) {
        // Authenticate the user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the lab and check if it exists
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Fetch the health package based on the provided package ID
        var healthPackageOptional = healthPackageRepository.findById(packageId);

        // Check if the health package exists
        if (healthPackageOptional.isEmpty()) {
            return ApiResponseHelper.errorResponse("Health package not found", HttpStatus.NOT_FOUND);
        }

        HealthPackage healthPackage = healthPackageOptional.get();

        // Check if the health package is associated with the lab
        if (!healthPackage.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("Health package not found", HttpStatus.NOT_FOUND);
        }

        // Fetch the health tests based on the provided test IDs
        List<Test> tests = testRepository.findAllById(packageRequest.getTestIds());  // Fetching tests by their IDs

        // Check if all test IDs exist in the database
        if (tests.size() != packageRequest.getTestIds().size()) {
            return ApiResponseHelper.errorResponse("One or more test IDs do not exist", HttpStatus.BAD_REQUEST);
        }

        // Update the health package
        healthPackage.setPackageName(packageRequest.getPackageName());
        healthPackage.setPrice(packageRequest.getPrice());
        healthPackage.setTests(new HashSet<>(tests));  // Convert List to Set before adding tests

        // Save the updated health package to the database
        healthPackageRepository.save(healthPackage);

        // Return the success response with the updated health package
        return ApiResponseHelper.successResponse(
                "Health package updated successfully",
                healthPackage
        );
    }



    //delete package
    @DeleteMapping("{labId}/package/{packageId}")
    public ResponseEntity<?> deleteHealthPackage(
            @PathVariable("labId") Long labId,
            @PathVariable("packageId") Long packageId,
            @RequestHeader("Authorization") String token
    ) {
        // Authenticate the user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the lab and check if it exists
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Fetch the package and check if it exists
        HealthPackage healthPackage = healthPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Health package not found"));

        // Check if the package is associated with the given lab
        if (!healthPackage.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("Health package is not associated with this lab", HttpStatus.BAD_REQUEST);
        }

        // Delete the health package
        healthPackageRepository.delete(healthPackage);

        // Return the success response
        return ApiResponseHelper.successResponse(
                "Health package deleted successfully",
                HttpStatus.OK
        );
    }




}
