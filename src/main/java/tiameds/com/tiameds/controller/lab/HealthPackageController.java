package tiameds.com.tiameds.controller.lab;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tiameds.com.tiameds.repository.HealthPackageRepository;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.repository.TestRepository;
import tiameds.com.tiameds.utils.UserAuthService;

@RestController
@RequestMapping("/admin/lab")
@Tag(name = "Package", description = "Package API which is used to manage packages")
public class HealthPackageController {

    // add test on package
    // remove test from package
    //get all packages
    //get package by id
    //get package by name
    //get package by test

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

    // create package


}
