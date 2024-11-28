package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tiameds.com.tiameds.entity.PatientEntity;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<PatientEntity> findAllByLabsId(Long labId);
}
