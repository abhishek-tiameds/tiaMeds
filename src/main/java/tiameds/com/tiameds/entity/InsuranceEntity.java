package tiameds.com.tiameds.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.HashSet;
import java.util.Set;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "insurance")
public class InsuranceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "insurance_id")
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int duration;

    @Column(name = "coverage_limit")
    private double coverageLimit;  // Optional

    @Column(name = "coverage_type")
    private String coverageType;  // Optional

    @Column(name = "status")
    private String status;  // Optional - e.g., Active, Inactive

    @Column(name = "provider")
    private String provider;  // Optional - Insurance provider name

    @Column(name = "created_at")
    @CreationTimestamp
    private String createdAt;  // Optional - Date of creation

    @Column(name = "updated_at")
    @UpdateTimestamp
    private String updatedAt;  // Optional - Date of last update

    @ManyToMany(mappedBy = "insurance", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonBackReference
    private Set<Lab> labs = new HashSet<>();

    public void setLab(Lab lab) {
        this.labs.add(lab);
        lab.getInsurance().add(this);
    }


}
