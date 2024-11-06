package tiameds.com.tiameds.services.lab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tiameds.com.tiameds.entity.User;
import tiameds.com.tiameds.repository.LabRepository;
import tiameds.com.tiameds.repository.UserRepository;

import java.util.Optional;

@Slf4j
@Service
public class UserLabService {

    private final UserRepository userRepository;
    private final LabRepository labRepository;

    public UserLabService(UserRepository userRepository, LabRepository labRepository) {
        this.userRepository = userRepository;
        this.labRepository = labRepository;
    }


    public Optional<User> findByUsername(String currentUsername) {
        return userRepository.findByUsername(currentUsername);
    }

    public boolean existsLabByName(String name) {
        return labRepository.existsByName(name);
    }
}
