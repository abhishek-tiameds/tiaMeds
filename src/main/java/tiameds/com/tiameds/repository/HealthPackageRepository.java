package tiameds.com.tiameds.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tiameds.com.tiameds.entity.HealthPackage;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthPackageRepository extends JpaRepository<HealthPackage, Long> {

}
