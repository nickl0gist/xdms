package pl.com.xdms.domain.reference;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.domain.customer.CustomerAgreement;
import pl.com.xdms.domain.manifest.ManifestPlan;
import pl.com.xdms.domain.supplier.SupplierAgreement;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Set;

@Entity
@Table(name = "refrences")
@Setter
@Getter
@ToString
public class Reference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long referenceID;

    @NotBlank
    @NotNull
    @Size(min = 4, max = 30)
    @Column(unique = true)
    private String number;

    @NotBlank
    @NotNull
    @Size(min = 5, max = 200)
    private String name;

    @NotBlank
    @NotNull
    @Size(min = 4, max = 30)
    private String hsCode;

    @NotBlank
    @NotNull
    @Min(0)
    private double weight;

    @NotBlank
    @NotNull
    @Min(0)
    private double weightPU;

    @NotBlank
    @NotNull
    @Min(0)
    private double weightHu;

    @NotBlank
    @NotNull
    @Min(1)
    @Max(6)
    private int stackability;

    @NotBlank
    @NotNull
    @Min(1)
    @Max(6)
    private int pcsPerPU;

    @NotBlank
    @NotNull
    @Min(1)
    @Max(6)
    private int pcsPerHU;

    @NotBlank
    @NotNull
    @Min(1)
    double palletWeight;

    @NotBlank
    @NotNull
    @Min(0)
    double palletHeight;

    @NotBlank
    @NotNull
    @Min(0)
    double palletLength;

    @NotNull
    @Column(columnDefinition = "BIT default true", nullable=false)
    private Boolean isActive;

    @NotBlank
    @NotNull
    @Size(min = 5, max = 200)
    private String designationEN;

    @Size(max = 200)
    private String designationDE;

    @NotNull
    @Size(max = 200)
    private String designationRU;

    @OneToMany
    @JoinTable(
            name = "reference_supplier_agreement",
            joinColumns = @JoinColumn(name = "referenceID"),
            inverseJoinColumns = @JoinColumn(name = "supplierAgreementID"))
    private Set<SupplierAgreement> supplierAgreements;

    @OneToMany
    @JoinTable(
            name = "reference_customer_agreement",
            joinColumns = @JoinColumn(name = "referenceID"),
            inverseJoinColumns = @JoinColumn(name = "customerAgreementID"))
    private Set<CustomerAgreement> customerAgreements;

    @OneToMany
    @JoinTable(
            name = "reference_storage_location",
            joinColumns = @JoinColumn(name = "referenceID"),
            inverseJoinColumns = @JoinColumn(name = "storageLocationID"))
    private Set<StorageLocation> storageLocations;

    @OneToMany(mappedBy = "reference")
    private Set<ManifestPlan> manifestsPlan;
}
