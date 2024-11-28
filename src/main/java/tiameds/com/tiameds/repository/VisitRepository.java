package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tiameds.com.tiameds.entity.VisitEntity;

@Repository
public interface VisitRepository extends JpaRepository<VisitEntity, Long> {

}
