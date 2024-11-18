package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tiameds.com.tiameds.entity.Test;

public interface TestRepository extends JpaRepository<Test, Long> {

    boolean existsByName(String name);
}