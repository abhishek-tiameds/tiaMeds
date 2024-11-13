package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tiameds.com.tiameds.entity.HealthPackage;

@Repository
public interface HealthPackageRepository extends JpaRepository<HealthPackage, Long> {

}
