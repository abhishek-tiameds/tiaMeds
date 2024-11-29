package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.PatientEntity;
import tiameds.com.tiameds.entity.VisitEntity;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<VisitEntity, Long> {

    Object findAllByPatient_Labs_Id(Long labId);

    List<VisitEntity> findAllByPatient_Labs(Lab lab);

    List<VisitEntity> findAllByPatient(PatientEntity patientEntity);

    @Query("SELECT v FROM VisitEntity v WHERE v.patient.patientId = :patientId")
    List<VisitEntity> findByPatientId(@Param("patientId") Long patientId);

}
