package tiameds.com.tiameds.controller.lab;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiameds.com.tiameds.dto.lab.DoctorDTO;
import tiameds.com.tiameds.entity.Doctors;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.DoctorRepository;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.services.lab.DoctorService;
import tiameds.com.tiameds.utils.ApiResponseHelper;
import tiameds.com.tiameds.utils.UserAuthService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/lab")
public class DoctorController {

    private final LabRepository labRepository;
    private final DoctorRepository doctorRepository;
    private final UserAuthService userAuthService;
    private final DoctorService doctorService;

    public DoctorController(LabRepository labRepository, DoctorRepository doctorRepository,
                            UserAuthService userAuthService, DoctorService doctorService) {
        this.labRepository = labRepository;
        this.doctorRepository = doctorRepository;
        this.userAuthService = userAuthService;
        this.doctorService = doctorService;
    }

    // Get all doctors of respective lab
    @GetMapping("{labId}/doctors")
    public ResponseEntity<?> getAllDoctors(
            @PathVariable("labId") Long labId,
            @RequestHeader("Authorization") String token) {

        // Authenticate user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the lab exists in the repository
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Retrieve and sort doctors by ID in ascending order
        List<DoctorDTO> doctors = lab.getDoctors().stream()
                .sorted(Comparator.comparingLong(Doctors::getId))
                .map(doctor -> new DoctorDTO(
                        doctor.getId(),
                        doctor.getName(),
                        doctor.getEmail(),
                        doctor.getSpeciality(),
                        doctor.getQualification(),
                        doctor.getHospitalAffiliation(),
                        doctor.getLicenseNumber(),
                        doctor.getPhone(),
                        doctor.getAddress(),
                        doctor.getCity(),
                        doctor.getState(),
                        doctor.getCountry(),
                        doctor.getCreatedAt(),
                        doctor.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        return ApiResponseHelper.successResponse("Doctors retrieved successfully", doctors);
    }

    // Get doctor by id
    @GetMapping("{labId}/doctors/{doctorId}")
    public ResponseEntity<?> getDoctorById(
            @PathVariable("labId") Long labId,
            @PathVariable("doctorId") Long doctorId,
            @RequestHeader("Authorization") String token) {

        // Authenticate user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the lab exists in the repository
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Retrieve the doctor by ID
        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Verify if the doctor is associated with the lab
        if (!doctor.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("Doctor is not associated with this lab", HttpStatus.UNAUTHORIZED);
        }

        // Map the doctor to a DTO
        DoctorDTO doctorDTO = new DoctorDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.getEmail(),
                doctor.getSpeciality(),
                doctor.getQualification(),
                doctor.getHospitalAffiliation(),
                doctor.getLicenseNumber(),
                doctor.getPhone(),
                doctor.getAddress(),
                doctor.getCity(),
                doctor.getState(),
                doctor.getCountry(),
                doctor.getCreatedAt(),
                doctor.getUpdatedAt()
        );

        return ApiResponseHelper.successResponse("Doctor retrieved successfully", doctorDTO);
    }

    // create doctor or add doctor to lab
    @PostMapping("{labId}/doctors")
    public ResponseEntity<?> addDoctorToLab(
            @PathVariable("labId") Long labId,
            @RequestBody DoctorDTO doctorDTO,
            @RequestHeader("Authorization") String token) {

        // Authenticate user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the lab exists in the repository
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        //check lab have doctor or not
        if (lab.getDoctors().size() > 0) {
            //check doctor already exist or not
            if (lab.getDoctors().stream().anyMatch(doctor -> doctor.getEmail().equals(doctorDTO.getEmail()))) {
                return ApiResponseHelper.errorResponse("Doctor already exists", HttpStatus.BAD_REQUEST);
            }
        }

        // Create a new doctor entity
        Doctors doctor = new Doctors();
        doctor.setName(doctorDTO.getName());
        doctor.setEmail(doctorDTO.getEmail());
        doctor.setSpeciality(doctorDTO.getSpeciality());
        doctor.setQualification(doctorDTO.getQualification());
        doctor.setHospitalAffiliation(doctorDTO.getHospitalAffiliation());
        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setPhone(doctorDTO.getPhone());
        doctor.setAddress(doctorDTO.getAddress());
        doctor.setCity(doctorDTO.getCity());
        doctor.setState(doctorDTO.getState());
        doctor.setCountry(doctorDTO.getCountry());
        doctor.getLabs().add(lab);
        doctorRepository.save(doctor);

        // Add the doctor to the lab
        lab.getDoctors().add(doctor);
        labRepository.save(lab);

        return ApiResponseHelper.successResponse("Doctor added successfully", doctorDTO);

    }


    // update doctor
    @PutMapping("{labId}/doctors/{doctorId}")
    public ResponseEntity<?> updateDoctor(
            @PathVariable("labId") Long labId,
            @PathVariable("doctorId") Long doctorId,
            @RequestBody DoctorDTO doctorDTO,
            @RequestHeader("Authorization") String token) {

        // Authenticate user
        User currentUser = userAuthService.authenticateUser(token)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the lab exists in the repository
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Verify if the current user is associated with the lab
        if (!currentUser.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Retrieve the doctor by ID
        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Verify if the doctor is associated with the lab
        if (!doctor.getLabs().contains(lab)) {
            return ApiResponseHelper.errorResponse("Doctor is not associated with this lab", HttpStatus.UNAUTHORIZED);
        }

        // Update the doctor entity
        doctor.setName(doctorDTO.getName());
        doctor.setEmail(doctorDTO.getEmail());
        doctor.setSpeciality(doctorDTO.getSpeciality());
        doctor.setQualification(doctorDTO.getQualification());
        doctor.setHospitalAffiliation(doctorDTO.getHospitalAffiliation());
        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setPhone(doctorDTO.getPhone());
        doctor.setAddress(doctorDTO.getAddress());
        doctor.setCity(doctorDTO.getCity());
        doctor.setState(doctorDTO.getState());
        doctor.setCountry(doctorDTO.getCountry());
        doctorRepository.save(doctor);

        return ApiResponseHelper.successResponse("Doctor updated successfully", doctorDTO);
    }

}
