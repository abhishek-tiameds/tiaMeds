package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.User;

import java.util.List;

public interface LabRepository extends JpaRepository<Lab, Long> {

    boolean existsByName(String name);

    List<Lab> findByCreatedBy(User currentUser);

}
