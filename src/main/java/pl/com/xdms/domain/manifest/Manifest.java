package pl.com.xdms.domain.manifest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import java.util.LinkedHashSet;
import java.util.Objects;
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
    @NotNull
    private Customer customer;

    @ManyToOne
    @JoinColumn(name="supplierID", nullable=false)
    @ToString.Exclude
    @NotNull
    private Supplier supplier;

    @OneToMany(mappedBy = "manifest")
    @JsonManagedReference
    @ToString.Exclude
    private Set<ManifestReference> manifestsReferenceSet;

    @OneToMany
    @JoinTable(
            name = "ttt_manifest",
            joinColumns = @JoinColumn(name = "manifestID"),
            inverseJoinColumns = @JoinColumn(name = "tttID"))
    @JsonBackReference
    private Set<TruckTimeTable> truckTimeTableSet = new LinkedHashSet<>();

    @OneToMany
    @JoinTable(
            name = "tpa_manifest",
            joinColumns = @JoinColumn(name = "manifest_id"),
            inverseJoinColumns = @JoinColumn(name = "tpaID"))
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "name"
    )
    private Set<TPA> tpaSet = new LinkedHashSet<>();

    @Transient
    Boolean isActive;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Manifest)) return false;
        Manifest manifest = (Manifest) o;
        return manifestCode.equals(manifest.manifestCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manifestCode);
    }
}
