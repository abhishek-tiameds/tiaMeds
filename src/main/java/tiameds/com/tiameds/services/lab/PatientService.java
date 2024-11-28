package tiameds.com.tiameds.services.lab;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tiameds.com.tiameds.dto.lab.BillingDTO;
import tiameds.com.tiameds.dto.lab.PatientDTO;
import tiameds.com.tiameds.dto.lab.VisitDTO;
import tiameds.com.tiameds.entity.*;
import tiameds.com.tiameds.repository.*;
import tiameds.com.tiameds.utils.ApiResponseHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final LabRepository labRepository;
    private final TestRepository testRepository;
    private final HealthPackageRepository healthPackageRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HealthPackageRepository packageRepository;
    private final InsuranceRepository insuranceRepository;
    private final BillingRepository billingRepository;


    public PatientService(LabRepository labRepository, TestRepository testRepository, HealthPackageRepository healthPackageRepository, PatientRepository patientRepository, DoctorRepository doctorRepository, HealthPackageRepository packageRepository, InsuranceRepository insuranceRepository, BillingRepository billingRepository) {
        this.labRepository = labRepository;
        this.testRepository = testRepository;
        this.healthPackageRepository = healthPackageRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.packageRepository = packageRepository;
        this.insuranceRepository = insuranceRepository;
        this.billingRepository = billingRepository;
    }

    @Transactional
    public void savePatientWithDetails(Lab lab, PatientDTO patientDTO) {

        if (patientRepository.existsByEmail(patientDTO.getPhone())) {
            ApiResponseHelper.errorResponse("Patient already exist", HttpStatus.BAD_REQUEST);
        }

        // Convert PatientDTO to PatientEntity
        PatientEntity patient = new PatientEntity();
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setEmail(patientDTO.getEmail());
        patient.setPhone(patientDTO.getPhone());
        patient.setAddress(patientDTO.getAddress());
        patient.setCity(patientDTO.getCity());
        patient.setState(patientDTO.getState());
        patient.setZip(patientDTO.getZip());
        patient.setBloodGroup(patientDTO.getBloodGroup());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());

        // Set the lab for the patient
        patient.getLabs().add(lab);

        // Handle visit details
        VisitDTO visitDTO = patientDTO.getVisit();
        if (visitDTO != null) {
            VisitEntity visit = new VisitEntity();
            visit.setVisitDate(visitDTO.getVisitDate());
            visit.setVisitType(visitDTO.getVisitType());
            visit.setVisitStatus(visitDTO.getVisitStatus());
            visit.setVisitDescription(visitDTO.getVisitDescription());

            //check doctors belong to the lab or not
            if (!lab.getDoctors().contains(doctorRepository.findById(visitDTO.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found")))) {
                ApiResponseHelper.errorResponse("Doctor not belong to the lab", HttpStatus.BAD_REQUEST);
            }

            // Associate doctor
            visit.setDoctor(doctorRepository.findById(visitDTO.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found")));

            //check test belong to the lab or not
            List<Test> tests = testRepository.findAllById(visitDTO.getTestIds());
            if (tests.stream().anyMatch(test -> !lab.getTests().contains(test))) {
                ApiResponseHelper.errorResponse("Test not belong to the lab", HttpStatus.BAD_REQUEST);
            }

            // Associate tests
            if (visitDTO.getTestIds() != null) {
                visit.setTests(new HashSet<>(testRepository.findAllById(visitDTO.getTestIds())));
            }

            //check health package belong to the lab or not
            List<HealthPackage> healthPackages = healthPackageRepository.findAllById(visitDTO.getPackageIds());
            if (healthPackages.stream().anyMatch(healthPackage -> !lab.getHealthPackages().contains(healthPackage))) {
                ApiResponseHelper.errorResponse("Health package not belong to the lab", HttpStatus.BAD_REQUEST);
            }


            // Associate packages
            if (visitDTO.getPackageIds() != null) {
                visit.setPackages(new HashSet<>(packageRepository.findAllById(visitDTO.getPackageIds())));
            }

            //check insurance belong to the lab or not
            List<InsuranceEntity> insurances = insuranceRepository.findAllById(visitDTO.getInsuranceIds().stream().map(Long::intValue).collect(Collectors.toList()));
            if (insurances.stream().anyMatch(insurance -> !lab.getInsurances().contains((CharSequence) insurance))) {
                ApiResponseHelper.errorResponse("Insurance not belong to the lab", HttpStatus.BAD_REQUEST);
            }

            // Associate insurance
            if (visitDTO.getInsuranceIds() != null) {
                List<Integer> insuranceIds = visitDTO.getInsuranceIds()
                        .stream()
                        .map(Long::intValue) // Convert Long to Integer
                        .toList(); // Create a new List of Integer

                visit.setInsurance(new HashSet<>(insuranceRepository.findAllById(insuranceIds)));
            }

            // Handle billing details
            BillingDTO billingDTO = visitDTO.getBilling();
            if (billingDTO != null) {
                BillingEntity billing = new BillingEntity();
                billing.setTotalAmount(billingDTO.getTotalAmount());
                billing.setPaymentStatus(billingDTO.getPaymentStatus());
                billing.setPaymentMethod(billingDTO.getPaymentMethod());
                billing.setPaymentDate(billingDTO.getPaymentDate());
                billing.setDiscount(billingDTO.getDiscount());
                billing.setGstRate(billingDTO.getGstRate());
                billing.setGstAmount(billingDTO.getGstAmount());
                billing.setCgstAmount(billingDTO.getCgstAmount());
                billing.setSgstAmount(billingDTO.getSgstAmount());
                billing.setIgstAmount(billingDTO.getIgstAmount());
                billing.setNetAmount(billingDTO.getNetAmount());

                visit.setBilling(billing);
            }

            // Link visit to patient
            visit.setPatient(patient);
            patient.getVisits().add(visit);
        }

        // Save patient and related entities
        patientRepository.save(patient);
    }


    public boolean existsByPhone(String phone) {
        return patientRepository.existsByPhone(phone);
    }


    //get all patients by lab id
    public List<PatientDTO> getAllPatientsByLabId(Long labId) {
        return patientRepository.findAllByLabsId(labId).stream()
                .map(patient -> {
                    PatientDTO patientDTO = new PatientDTO();
                    patientDTO.setFirstName(patient.getFirstName());
                    patientDTO.setLastName(patient.getLastName());
                    patientDTO.setEmail(patient.getEmail());
                    patientDTO.setPhone(patient.getPhone());
                    patientDTO.setAddress(patient.getAddress());
                    patientDTO.setCity(patient.getCity());
                    patientDTO.setState(patient.getState());
                    patientDTO.setZip(patient.getZip());
                    patientDTO.setBloodGroup(patient.getBloodGroup());
                    patientDTO.setDateOfBirth(patient.getDateOfBirth());
                    return patientDTO;
                })
                .collect(Collectors.toList());
    }


    //get patient by id of the lab
    public Object getPatientById(Long patientId, Long labId) {

        // Get the lab by ID
        Optional<Lab> lab = labRepository.findById(labId);
        if (lab.isEmpty()) {
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the patient exists and belongs to the given lab
        Optional<PatientEntity> patient = patientRepository.findById(patientId);
        if (patient.isEmpty() || !patient.get().getLabs().contains(lab.get())) {
            return ApiResponseHelper.errorResponse("Patient not found for the specified lab", HttpStatus.NOT_FOUND);
        }

        // Make the response
        PatientEntity patientEntity = patient.get();
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName(patientEntity.getFirstName());
        patientDTO.setLastName(patientEntity.getLastName());
        patientDTO.setEmail(patientEntity.getEmail());
        patientDTO.setPhone(patientEntity.getPhone());
        patientDTO.setAddress(patientEntity.getAddress());
        patientDTO.setCity(patientEntity.getCity());
        patientDTO.setState(patientEntity.getState());
        patientDTO.setZip(patientEntity.getZip());
        patientDTO.setBloodGroup(patientEntity.getBloodGroup());
        patientDTO.setDateOfBirth(patientEntity.getDateOfBirth());

        return patientDTO;
    }

    public boolean existsById(Long patientId) {
        return patientRepository.existsById(patientId);
    }


    public void updatePatient(Long patientId, Long labId, PatientDTO patientDTO) {
        // Check if the lab exists
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Check if the patient exists and belongs to the lab
        PatientEntity patientEntity = patientRepository.findById(patientId)
                .filter(patient -> patient.getLabs().stream()
                        .anyMatch(existingLab -> Objects.equals(existingLab.getId(), labId)))
                .orElseThrow(() -> new RuntimeException("Patient not found for the specified lab"));

        // Update the patient details from the DTO
        patientEntity.setFirstName(patientDTO.getFirstName());
        patientEntity.setLastName(patientDTO.getLastName());
        patientEntity.setEmail(patientDTO.getEmail());
        patientEntity.setPhone(patientDTO.getPhone());
        patientEntity.setAddress(patientDTO.getAddress());
        patientEntity.setCity(patientDTO.getCity());
        patientEntity.setState(patientDTO.getState());
        patientEntity.setZip(patientDTO.getZip());
        patientEntity.setBloodGroup(patientDTO.getBloodGroup());
        patientEntity.setDateOfBirth(patientDTO.getDateOfBirth());

        // Update lab relationship (optional, if labs can be updated)
        patientEntity.getLabs().clear();
        patientEntity.getLabs().add(lab);

        // Save the updated patient
        patientRepository.save(patientEntity);
    }

    public void deletePatient(Long patientId, Long labId) {
        // Check if the lab exists
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));

        // Check if the patient exists and belongs to the lab
        PatientEntity patientEntity = patientRepository.findById(patientId)
                .filter(patient -> patient.getLabs().stream()
                        .anyMatch(existingLab -> Objects.equals(existingLab.getId(), labId)))
                .orElseThrow(() -> new RuntimeException("Patient not found for the specified lab"));

        // Delete the patient
        patientRepository.delete(patientEntity);
    }
}
