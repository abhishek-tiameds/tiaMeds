package tiameds.com.tiameds.services.auth;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tiameds.com.tiameds.entity.Role;
import tiameds.com.tiameds.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
public class MyUserDetails implements UserDetails {

    private final User user;



    // Constructor
    public MyUserDetails(User user) {
        this.user = user;
    }

    // Method to access the User entity
    public User getUser() {
        return user;
    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        Set<Role> roles = user.getRoles();
//        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//
//        for (Role role : roles) {
//            authorities.add(new SimpleGrantedAuthority(role.getName()));
//        }
//
//        return authorities;
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (Role role : roles) {
            // Ensure role names are prefixed with 'ROLE_'
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        return authorities;
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Implement based on your business logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Implement based on your business logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Implement based on your business logic
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }


}
