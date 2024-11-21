package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tiameds.com.tiameds.entity.Test;


@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    boolean existsByName(String name);
}