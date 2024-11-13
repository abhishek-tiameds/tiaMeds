package tiameds.com.tiameds.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "health_packages")
public class HealthPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "package_name")
    @NotNull(message = "Package name is required")
    @Size(min = 3, max = 100, message = "Package name must be between 3 and 100 characters")
    private String name;

    @Column(name = "package_description")
    @NotNull(message = "Package description is required")
    @Size(min = 5, max = 500, message = "Package description must be between 5 and 500 characters")
    private String description;

    @Column(name = "package_status")
    @NotNull(message = "Package status is required")
    private Boolean status;

    @Column(name = "package_price")
    @NotNull(message = "Package price is required")
    private Integer price;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // List of tests in the package
    @ManyToMany
    @JoinTable(
            name = "package_tests",
            joinColumns = @JoinColumn(name = "package_id"),
            inverseJoinColumns = @JoinColumn(name = "test_id")
    )
    private Set<Test> tests = new HashSet<>();

    // Created by which lab
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    @JsonBackReference
    private Lab lab;
}
