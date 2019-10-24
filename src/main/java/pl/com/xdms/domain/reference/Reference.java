package pl.com.xdms.domain.reference;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.customer.CustomerAgreement;
import pl.com.xdms.domain.supplier.SupplierAgreement;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Set;

@Entity
@Table(name = "REFERENCE")
@Setter
@Getter
@ToString
public class Reference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long referenceID;

    @NotBlank
    @NotNull
    @Size(min = 4, max = 30)
    @Column(unique = true)
    private String number;

    @NotBlank
    @NotNull
    @Size(min = 5, max = 200)
    @Pattern(regexp = "^[A-Z0-9-]+$")
    private String name;

    @NotBlank
    @NotNull
    @Size(min = 4, max = 30)
    private String hsCode;

    @NotNull
    @Min(0)
    private double weight;

    @NotNull
    @Min(0)
    private double weightPu;

    @NotNull
    @Min(0)
    private double weightHu;

    @NotNull
    @Min(1)
    @Max(6)
    private int stackability;

    @NotNull
    @Min(1)
    private int pcsPerPU;

    @NotNull
    @Min(1)
    private int pcsPerHU;

    @NotNull
    @Min(1)
    double palletWeight;

    @NotNull
    @Min(0)
    int palletHeight;

    @NotNull
    @Min(0)
    int palletLength;

    @NotNull
    @Min(0)
    int palletWidth;

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
    @JsonManagedReference
    private Set<SupplierAgreement> supplierAgreements;

    @OneToMany
    @JoinTable(
            name = "reference_customer_agreement",
            joinColumns = @JoinColumn(name = "referenceID"),
            inverseJoinColumns = @JoinColumn(name = "customerAgreementID"))
    @JsonManagedReference
    //@JsonIgnoreProperties({"customer"})
    private Set<CustomerAgreement> customerAgreements;

/*    @OneToMany
    @JoinTable(
            name = "reference_storage_location",
            joinColumns = @JoinColumn(name = "referenceID"),
            inverseJoinColumns = @JoinColumn(name = "storageLocationID"))
    @JsonManagedReference
    private Set<StorageLocation> storageLocations;*/

/*    @OneToMany(mappedBy = "reference")
    @JsonIgnore
    private Set<ManifestPlan> manifestsPlan;*/
}
