package tiameds.com.tiameds.services.lab;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tiameds.com.tiameds.dto.lab.BillingDTO;
import tiameds.com.tiameds.dto.lab.PatientDTO;
import tiameds.com.tiameds.dto.lab.VisitDTO;
import tiameds.com.tiameds.entity.*;
import tiameds.com.tiameds.repository.*;
import tiameds.com.tiameds.utils.ApiResponseHelper;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class VisitService {

    private final PatientRepository patientRepository;
    private final LabRepository labRepository;
    private final TestRepository testRepository;
    private final HealthPackageRepository healthPackageRepository;
    private final DoctorRepository doctorRepository;
    private final InsuranceRepository insuranceRepository;
    private final BillingRepository billingRepository;
    private final VisitRepository visitRepository;

    public VisitService(PatientRepository patientRepository,
                        LabRepository labRepository,
                        TestRepository testRepository,
                        HealthPackageRepository healthPackageRepository,
                        DoctorRepository doctorRepository,
                        InsuranceRepository insuranceRepository,
                        BillingRepository billingRepository,
                        VisitRepository visitRepository) {
        this.patientRepository = patientRepository;
        this.labRepository = labRepository;
        this.testRepository = testRepository;
        this.healthPackageRepository = healthPackageRepository;
        this.doctorRepository = doctorRepository;
        this.insuranceRepository = insuranceRepository;
        this.billingRepository = billingRepository;
        this.visitRepository = visitRepository;
    }

    @Transactional
    public void addVisit(Long labId, Long patientId, VisitDTO visitDTO, Optional<User> currentUser) {

        // Check if the lab exists
        Optional<Lab> labOptional = labRepository.findById(labId);
        if (labOptional.isEmpty()) {
            ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the user is a member of the lab
        if (!currentUser.get().getLabs().contains(labOptional.get())) {
            ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Check if the patient belongs to the lab
        Optional<PatientEntity> patientEntity = patientRepository.findById(patientId)
                .filter(patient -> patient.getLabs().contains(labOptional.get()));
        if (patientEntity.isEmpty()) {
            ApiResponseHelper.errorResponse("Patient not belong to the lab", HttpStatus.BAD_REQUEST);
        }

        // Check if the doctor exists
        Optional<Doctors> doctorOptional = doctorRepository.findById(visitDTO.getDoctorId());
        if (doctorOptional.isEmpty()) {
            ApiResponseHelper.errorResponse("Doctor not found", HttpStatus.NOT_FOUND);
        }

        // Create the visit entity
        VisitEntity visit = new VisitEntity();
        visit.setPatient(patientEntity.get());
        visit.setVisitDate(visitDTO.getVisitDate());
        visit.setVisitType(visitDTO.getVisitType());
        visit.setVisitStatus(visitDTO.getVisitStatus());
        visit.setVisitDescription(visitDTO.getVisitDescription());
        visit.setDoctor(doctorOptional.get());

        // Set tests
        Set<Test> tests = testRepository.findAllById(visitDTO.getTestIds()).stream().collect(Collectors.toSet());
        visit.setTests(tests);

        // Set health packages
        Set<HealthPackage> healthPackages = healthPackageRepository.findAllById(visitDTO.getPackageIds()).stream().collect(Collectors.toSet());
        visit.setPackages(healthPackages);

        // Set insurances
        List<InsuranceEntity> insurances = insuranceRepository.findAllById(visitDTO.getInsuranceIds().stream().map(Long::intValue).collect(Collectors.toList()));
        if (insurances.stream().anyMatch(insurance -> !insurance.getLabs().contains(labOptional.get()))) {
            ApiResponseHelper.errorResponse("Insurance not belong to the lab", HttpStatus.BAD_REQUEST);
        }
        visit.setInsurance(new HashSet<>(insurances));

        // Handle billing information
        BillingEntity billingEntity = new BillingEntity();
        billingEntity.setTotalAmount(visitDTO.getBilling().getTotalAmount());
        billingEntity.setPaymentStatus(visitDTO.getBilling().getPaymentStatus());
        billingEntity.setPaymentMethod(visitDTO.getBilling().getPaymentMethod());
        billingEntity.setPaymentDate(visitDTO.getBilling().getPaymentDate());
        billingEntity.setDiscount(visitDTO.getBilling().getDiscount());
        billingEntity.setGstRate(visitDTO.getBilling().getGstRate());
        billingEntity.setGstAmount(visitDTO.getBilling().getGstAmount());
        billingEntity.setCgstAmount(visitDTO.getBilling().getCgstAmount());
        billingEntity.setSgstAmount(visitDTO.getBilling().getSgstAmount());
        billingEntity.setIgstAmount(visitDTO.getBilling().getIgstAmount());
        billingEntity.setNetAmount(visitDTO.getBilling().getNetAmount());

        billingRepository.save(billingEntity);
        visit.setBilling(billingEntity);

        // Save the visit
        visitRepository.save(visit);


    }


    // get list of patient visits of respective lab
    public Object getVisits(Long labId, Optional<User> currentUser) {
        // Check if the lab exists
        Optional<Lab> labOptional = labRepository.findById(labId);
        if (labOptional.isEmpty()) {
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the user is a member of the lab
        if (currentUser.isEmpty() || !currentUser.get().getLabs().contains(labOptional.get())) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Get the list of visits
        List<VisitEntity> visits = visitRepository.findAllByPatient_Labs(labOptional.get());

        // Map visits to PatientDTO
        List<PatientDTO> patientDTOList = visits.stream()
                .map(this::mapVisitToPatientDTO)
                .collect(Collectors.toList());

        return patientDTOList;
    }

    // Helper Method to Map VisitEntity to PatientDTO
    private PatientDTO mapVisitToPatientDTO(VisitEntity visit) {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName(visit.getPatient().getFirstName());
        patientDTO.setLastName(visit.getPatient().getLastName());
        patientDTO.setEmail(visit.getPatient().getEmail());
        patientDTO.setPhone(visit.getPatient().getPhone());
        patientDTO.setAddress(visit.getPatient().getAddress());
        patientDTO.setCity(visit.getPatient().getCity());
        patientDTO.setState(visit.getPatient().getState());
        patientDTO.setZip(visit.getPatient().getZip());
        patientDTO.setBloodGroup(visit.getPatient().getBloodGroup());
        patientDTO.setDateOfBirth(visit.getPatient().getDateOfBirth());

        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setVisitDate(visit.getVisitDate());
        visitDTO.setVisitType(visit.getVisitType());
        visitDTO.setVisitStatus(visit.getVisitStatus());
        visitDTO.setVisitDescription(visit.getVisitDescription());
        visitDTO.setDoctorId(visit.getDoctor().getId());
        visitDTO.setTestIds(visit.getTests().stream().map(Test::getId).collect(Collectors.toList()));
        visitDTO.setPackageIds(visit.getPackages().stream().map(HealthPackage::getId).collect(Collectors.toList()));
        visitDTO.setInsuranceIds(visit.getInsurance().stream().map(InsuranceEntity::getId).collect(Collectors.toList()));

        BillingDTO billingDTO = new BillingDTO();
        billingDTO.setTotalAmount(visit.getBilling().getTotalAmount());
        billingDTO.setPaymentStatus(visit.getBilling().getPaymentStatus());
        billingDTO.setPaymentMethod(visit.getBilling().getPaymentMethod());
        billingDTO.setPaymentDate(visit.getBilling().getPaymentDate());
        billingDTO.setDiscount(visit.getBilling().getDiscount());
        billingDTO.setGstRate(visit.getBilling().getGstRate());
        billingDTO.setGstAmount(visit.getBilling().getGstAmount());
        billingDTO.setCgstAmount(visit.getBilling().getCgstAmount());
        billingDTO.setSgstAmount(visit.getBilling().getSgstAmount());
        billingDTO.setIgstAmount(visit.getBilling().getIgstAmount());
        billingDTO.setNetAmount(visit.getBilling().getNetAmount());

        visitDTO.setBilling(billingDTO);
        patientDTO.setVisit(visitDTO);

        return patientDTO;
    }


    @Transactional
    public void updateVisit(Long labId, Long visitId, VisitDTO visitDTO, Optional<User> currentUser) {

        // Check if the lab exists
        Optional<Lab> labOptional = labRepository.findById(labId);
        if (labOptional.isEmpty()) {
            ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the user is a member of the lab
        if (currentUser.isEmpty() || !currentUser.get().getLabs().contains(labOptional.get())) {
            ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        VisitEntity visit = visitRepository.findById(visitId)
                .filter(visitEntity -> visitEntity.getPatient().getLabs().contains(labOptional.get()))
                .orElseThrow(() -> new IllegalArgumentException("Visit not found or does not belong to the lab"));


        Optional<Doctors> doctorOptional = doctorRepository.findById(visitDTO.getDoctorId());
        if (doctorOptional.isEmpty()) {
            ApiResponseHelper.errorResponse("Doctor not found", HttpStatus.NOT_FOUND);
        }

        // Update the visit
        visit.setVisitDate(visitDTO.getVisitDate());
        visit.setVisitType(visitDTO.getVisitType());
        visit.setVisitStatus(visitDTO.getVisitStatus());
        visit.setVisitDescription(visitDTO.getVisitDescription());
        visit.setDoctor(doctorOptional.get());

        // Set tests
        Set<Test> tests = testRepository.findAllById(visitDTO.getTestIds()).stream().collect(Collectors.toSet());
        visit.setTests(tests);

        // Set health packages
        Set<HealthPackage> healthPackages = healthPackageRepository.findAllById(visitDTO.getPackageIds()).stream().collect(Collectors.toSet());
        visit.setPackages(healthPackages);

        // Set insurances
        List<InsuranceEntity> insurances = insuranceRepository.findAllById(visitDTO.getInsuranceIds().stream().map(Long::intValue).collect(Collectors.toList()));
        if (insurances.stream().anyMatch(insurance -> !insurance.getLabs().contains(labOptional.get()))) {
            ApiResponseHelper.errorResponse("Insurance not belong to the lab", HttpStatus.BAD_REQUEST);
        }
        visit.setInsurance(new HashSet<>(insurances));

        // Handle billing information
        BillingEntity billingEntity = visit.getBilling();
        billingEntity.setTotalAmount(visitDTO.getBilling().getTotalAmount());
        billingEntity.setPaymentStatus(visitDTO.getBilling().getPaymentStatus());
        billingEntity.setPaymentMethod(visitDTO.getBilling().getPaymentMethod());
        billingEntity.setPaymentDate(visitDTO.getBilling().getPaymentDate());
        billingEntity.setDiscount(visitDTO.getBilling().getDiscount());
        billingEntity.setGstRate(visitDTO.getBilling().getGstRate());
        billingEntity.setGstAmount(visitDTO.getBilling().getGstAmount());

        billingEntity.setCgstAmount(visitDTO.getBilling().getCgstAmount());
        billingEntity.setSgstAmount(visitDTO.getBilling().getSgstAmount());
        billingEntity.setIgstAmount(visitDTO.getBilling().getIgstAmount());
        billingEntity.setNetAmount(visitDTO.getBilling().getNetAmount());

        billingRepository.save(billingEntity);
        visit.setBilling(billingEntity);
        // Save the visit
        visitRepository.save(visit);

    }


    // delete the visit
    public void deleteVisit(Long labId, Long visitId, Optional<User> currentUser) {
        // Check if the lab exists
        Optional<Lab> labOptional = labRepository.findById(labId);
        if (labOptional.isEmpty()) {
            ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the user is a member of the lab
        if (currentUser.isEmpty() || !currentUser.get().getLabs().contains(labOptional.get())) {
            ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Check if the visit exists
        Optional<VisitEntity> visitOptional = visitRepository.findById(visitId);
        if (visitOptional.isEmpty()) {
            ApiResponseHelper.errorResponse("Visit not found", HttpStatus.NOT_FOUND);
        }

        VisitEntity visit = visitRepository.findById(visitId)
                .filter(visitEntity -> visitEntity.getPatient().getLabs().contains(labOptional.get()))
                .orElseThrow(() -> new IllegalArgumentException("Visit not found or does not belong to the lab"));

        visitRepository.delete(visit);
    }


    // get the visit details
    public Object getVisit(Long labId, Long visitId, Optional<User> currentUser) {
        // Check if the lab exists
        Optional<Lab> labOptional = labRepository.findById(labId);
        if (labOptional.isEmpty()) {
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the user is a member of the lab
        if (currentUser.isEmpty() || !currentUser.get().getLabs().contains(labOptional.get())) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Check if the visit exists
        Optional<VisitEntity> visitOptional = visitRepository.findById(visitId);
        if (visitOptional.isEmpty()) {
            return ApiResponseHelper.errorResponse("Visit not found", HttpStatus.NOT_FOUND);
        }

        VisitEntity visit = visitRepository.findById(visitId)
                .filter(visitEntity -> visitEntity.getPatient().getLabs().contains(labOptional.get()))
                .orElseThrow(() -> new IllegalArgumentException("Visit not found or does not belong to the lab"));


        // Map visit to PatientDTO
        PatientDTO patientDTO = mapVisitToPatientDTO(visit);

        return patientDTO;
    }

    public Object getVisitByPatient(Long labId, Long patientId, Optional<User> currentUser) {
        // Check if the lab exists
        Optional<Lab> labOptional = labRepository.findById(labId);
        if (labOptional.isEmpty()) {
            return ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the user is a member of the lab
        if (currentUser.isEmpty() || !currentUser.get().getLabs().contains(labOptional.get())) {
            return ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Check if the patient belongs to the lab
        Optional<PatientEntity> patientEntity = patientRepository.findById(patientId)
                .filter(patient -> patient.getLabs().contains(labOptional.get()));
        if (patientEntity.isEmpty()) {
            return ApiResponseHelper.errorResponse("Patient not belong to the lab", HttpStatus.BAD_REQUEST);
        }


        // Get the list of visits
        List<VisitEntity> visits = visitRepository.findAllByPatient(patientEntity.get());

        // Map visits to PatientDTO
        List<PatientDTO> patientDTOList = visits.stream()
                .map(this::mapVisitToPatientDTO)
                .collect(Collectors.toList());

        return patientDTOList;
    }
}

