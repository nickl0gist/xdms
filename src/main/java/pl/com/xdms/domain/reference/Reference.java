package pl.com.xdms.domain.reference;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.domain.supplier.Supplier;

import javax.persistence.*;
import javax.validation.constraints.*;

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
    private double weightOfPackaging;

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
    @Column(columnDefinition = "BIT default true", nullable = false)
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

    @Size(max = 200)
    @NotNull
    @Column(unique = true)
    @Pattern(regexp = "^[0-9]+$")
    private String supplierAgreement;

    @Size(max = 200)
    @NotNull
    @Column(unique = true)
    @Pattern(regexp = "^[0-9]+$")
    private String customerAgreement;

    @NotNull
    @Size(max = 50)
    @ManyToOne
    @JoinColumn
    @JsonManagedReference
    @ToString.Exclude
    private Customer customer;

    @NotBlank
    @NotNull
    @Size(max = 50)
    @ManyToOne
    @JoinColumn
    @JsonManagedReference
    @ToString.Exclude
    private Supplier supplier;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "storage_locationid")
    private StorageLocation storageLocation;

    public String toStringForExcel(String fieldDivider) {

        return referenceID +
                fieldDivider + number +
                fieldDivider + name +
                fieldDivider + designationEN +
                fieldDivider + designationRU +
                fieldDivider + hsCode +
                fieldDivider + weight +
                fieldDivider + weightOfPackaging +
                fieldDivider + stackability +
                fieldDivider + pcsPerPU +
                fieldDivider + pcsPerHU +
                fieldDivider + palletWeight +
                fieldDivider + palletHeight +
                fieldDivider + palletLength +
                fieldDivider + palletWidth +
                fieldDivider + supplier.getName() +
                fieldDivider + supplierAgreement +
                fieldDivider + customer.getName() +
                fieldDivider + customerAgreement +
                fieldDivider + storageLocation.getCode() +
                fieldDivider + isActive;
    }
}


    /*@OneToMany
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
    private Set<CustomerAgreement> customerAgreements;*/

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

