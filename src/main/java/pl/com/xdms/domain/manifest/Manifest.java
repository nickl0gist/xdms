package pl.com.xdms.domain.manifest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Set;

@Entity
@Table(name ="manifests")
@Setter
@Getter
@ToString
public class Manifest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long manifestID;

    @NotBlank
    @NotNull
    @Column(unique = true)
    private String manifestCode;

    @NotBlank
    @Min(1)
    private int palletQty;

    @NotBlank
    @Min(0)
    private double totalWeight;

    @NotBlank
    @Min(0)
    private double totalLDM;

    @ManyToOne
    @JoinColumn(name="customerID", nullable=false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name="supplierID", nullable=false)
    private Supplier supplier;

    @OneToMany(mappedBy = "manifest")
    private Set<ManifestPlan> manifestsPlan;

    @OneToMany
    @JoinTable(
            name = "ttt_manifest",
            joinColumns = @JoinColumn(name = "manifestID"),
            inverseJoinColumns = @JoinColumn(name = "tttID"))
    private Set<TruckTimeTable> truckTimeTables;

}
