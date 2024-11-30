package tiameds.com.tiameds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.entity.Test;

import java.util.List;


@Repository
public interface TestRepository extends JpaRepository<Test, Long> {


    List<Test> findByLabs(Lab lab);
}