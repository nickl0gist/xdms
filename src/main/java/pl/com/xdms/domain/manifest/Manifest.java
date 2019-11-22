package pl.com.xdms.domain.manifest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manifestID;

    @NotBlank
    @NotNull
    @Column(unique = true)
    private String manifestCode;

    @NotBlank
    @Min(0)
    @Column(name = "pallet_qty_planned")
    private int palletQtyPlanned;

    @NotBlank
    @Min(0)
    @Column(name = "box_qty_planned")
    private int boxQtyPlanned;

    @NotBlank
    @Min(0)
    @Column(name = "total_weight_planned")
    private double totalWeightPlanned;

    @NotBlank
    @Min(0)
    @Column(name = "totalldm_planned")
    private double totalLdmPlanned;

    @Min(0)
    @Column(name = "pallet_qty_real")
    private int palletQtyReal;

    @Min(0)
    @Column(name = "box_qty_real")
    private int boxQtyReal;

    @Min(0)
    @Column(name = "total_weight_real")
    private double totalWeightReal;

    @Min(0)
    @Column(name = "totalldm_real")
    private double totalLdmReal;

    @ManyToOne
    @JoinColumn(name="customerID", nullable=false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name="supplierID", nullable=false)
    private Supplier supplier;

    @OneToMany(mappedBy = "manifest")
    @JsonManagedReference
    private Set<ManifestPlan> manifestsPlan;

    @ManyToOne
    @JoinTable(
            name = "ttt_manifest",
            joinColumns = @JoinColumn(name = "manifestID"),
            inverseJoinColumns = @JoinColumn(name = "tttID"))
    @JsonBackReference
    private TruckTimeTable truckTimeTables;

}
