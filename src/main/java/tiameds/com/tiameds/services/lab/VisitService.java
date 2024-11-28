package tiameds.com.tiameds.services.lab;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // list all visits for a lab
    public Object getVisits(Long labId, Optional<User> currentUser) {

        // Check if the lab exists
        Optional<Lab> labOptional = labRepository.findById(labId);
        if (labOptional.isEmpty()) {
            ApiResponseHelper.errorResponse("Lab not found", HttpStatus.NOT_FOUND);
        }

        // Check if the user is a member of the lab
        if (!currentUser.get().getLabs().contains(labOptional.get())) {
            ApiResponseHelper.errorResponse("User is not a member of this lab", HttpStatus.UNAUTHORIZED);
        }

        // Get all visits for the lab
        List <VisitEntity> visits = (List<VisitEntity>) visitRepository.findAllByPatient_Labs_Id(labId);
        return visits;

    }

    public String getAllVisits(Long labId) {

        return null;
    }
}

