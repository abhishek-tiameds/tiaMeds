package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tiameds.com.tiameds.entity.BillingEntity;

public interface BillingRepository extends JpaRepository<BillingEntity, Long> {

}
