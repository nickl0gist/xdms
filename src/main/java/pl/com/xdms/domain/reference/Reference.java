package pl.com.xdms.domain.reference;

import lombok.*;
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
@NoArgsConstructor
@AllArgsConstructor
public class Reference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long referenceID;

    @NotBlank (message = "{validation.notblank.message}")
    @NotNull (message = "{validation.notnull.message}")
    @Size(min = 4, max = 30, message = "{validation.reference.number.pattern}")
    @Pattern(regexp = "^[A-Z0-9-]+", message = "{validation.reference.number.message}")
    private String number;

    @NotBlank (message = "{validation.reference.notblank}")
    @NotNull (message = "{validation.reference.notnull}")
    @Size(min = 5, max = 200, message = "{validation.reference.name.size}")
    private String name;

    @NotBlank (message = "{validation.notblank.message}")
    @NotNull (message = "{validation.notnull.message}")
    @Size(min = 4, message = "{validation.reference.hscode.size}")
    @Pattern(regexp = "^[0-9]+", message = "{validation.reference.hsCode}")
    private String hsCode;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 0, message = "{validation.minimal.zero}")
    private double weight;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 0, message = "{validation.minimal.zero }")
    private double weightOfPackaging;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 1, message = "{validation.reference.min.1}")
    @Max(value = 6, message = "{validation.reference.max.6}")
    private int stackability;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 1, message = "{validation.reference.min.pcsPerPu}")
    private int pcsPerPU;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 1, message = "{validation.reference.min.pcsPerHu}")
    private int puPerHU;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 1, message = "{validation.reference.min.palletWeight}")
    double palletWeight;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 0, message = "{validation.minimal.zero }")
    int palletHeight;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 0, message = "{validation.minimal.zero }")
    int palletLength;

    @NotNull (message = "{validation.notnull.message}")
    @Min(value = 0, message = "{validation.minimal.zero }")
    int palletWidth;

    @NotNull (message = "{validation.notnull.message}")
    @Column(columnDefinition = "BIT default true", nullable = false)
    private Boolean isActive;

    @NotBlank (message = "{validation.notblank.message}")
    @NotNull (message = "{validation.notnull.message}")
    @Size(min = 5, max = 200, message = "{filed.reference.designation}")
    private String designationEN;

    @Size(max = 200, message = "{filed.reference.designation}")
    private String designationDE;

    @NotNull (message = "{validation.notnull.message}")
    @Size(max = 200, message = "{filed.reference.designation}")
    private String designationRU;

    @Size(max = 200)
    @NotNull (message = "{validation.notnull.message}")
    @Column(unique = true)
    @Pattern(regexp = "^[0-9]+", message = "{validation.reference.agreement.message}")
    private String supplierAgreement;

    @Size(max = 200)
    @NotNull (message = "{validation.notnull.message}")
    @Column(unique = true)
    @Pattern(regexp = "^[0-9]+", message = "{validation.reference.agreement.message}")
    private String customerAgreement;

    @NotNull (message = "{validation.notnull.message}")
    @ManyToOne
    @JoinColumn
    @ToString.Exclude
    private Customer customer;

    @NotNull (message = "{validation.notnull.message}")
    @ManyToOne
    @JoinColumn
    @ToString.Exclude
    private Supplier supplier;

    @NotNull (message = "{validation.notnull.message}")
    @ManyToOne
    @JoinColumn(name = "storage_locationid")
    private StorageLocation storageLocation;
}