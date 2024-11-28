package tiameds.com.tiameds.services.lab;

import org.springframework.stereotype.Service;
import tiameds.com.tiameds.entity.User;

import java.util.Optional;

@Service
public class BillingService {
    public void updateBillingStatus(Long labId, Long visitId, Optional<User> currentUser) {

        //check if the user is authorized to update the billing status
    }
}
