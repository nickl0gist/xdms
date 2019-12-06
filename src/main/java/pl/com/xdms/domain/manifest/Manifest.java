package pl.com.xdms.domain.manifest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Entity
@Table(name ="manifests")
@Setter
@Getter
@ToString
public class Manifest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manifestID;

    @NotBlank
    @NotNull
    @Column(unique = true)
    private String manifestCode;

    @Min(0)
    @Column
    private int palletQtyPlanned;

    @Min(0)
    @Column
    private int boxQtyPlanned;

    @Min(0)
    private double totalWeightPlanned;

    @Min(0)
    @Column(name = "totalldm_planned")
    private double totalLdmPlanned;

    @Min(0)
    private int palletQtyReal;

    @Min(0)
    private int boxQtyReal;

    @Min(0)
    private double totalWeightReal;

    @Min(0)
    @Column(name = "totalldm_real")
    private double totalLdmReal;

    @ManyToOne
    @JoinColumn(name="customerID", nullable=false)
    @ToString.Exclude
    private Customer customer;

    @ManyToOne
    @JoinColumn(name="supplierID", nullable=false)
    @ToString.Exclude
    private Supplier supplier;

    @OneToMany(mappedBy = "manifest")
    @JsonManagedReference
    private Set<ManifestReference> manifestsReferenceSet;

    @OneToMany
    @JoinTable(
            name = "ttt_manifest",
            joinColumns = @JoinColumn(name = "manifestID"),
            inverseJoinColumns = @JoinColumn(name = "tttID"))
    @JsonBackReference
    private List<TruckTimeTable> truckTimeTables;

    @OneToMany
    @JoinTable(
            name = "tpa_manifest",
            joinColumns = @JoinColumn(name = "manifest_id"),
            inverseJoinColumns = @JoinColumn(name = "tpaID"))
    @JsonBackReference
    private List<TPA> tpa;

    @Transient
    Boolean isActive;
}
