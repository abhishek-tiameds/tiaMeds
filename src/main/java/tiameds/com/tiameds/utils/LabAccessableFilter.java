package tiameds.com.tiameds.utils;

import org.springframework.stereotype.Service;
import tiameds.com.tiameds.entity.Lab;
import tiameds.com.tiameds.repository.LabRepository;

@Service
public class LabAccessableFilter {

    private final LabRepository labRepository;

    public LabAccessableFilter(LabRepository labRepository) {
        this.labRepository = labRepository;
    }

    /**
     * Checks if a lab is accessible by ensuring it exists and is active.
     *
     * @param labId the ID of the lab
     * @return true if the lab exists and is active, false otherwise
     */
    public boolean isLabAccessible(Long labId) {
        return labRepository.findById(labId)
                .filter(Lab::getIsActive)
                .isPresent();
    }
}
