package pl.com.xdms.domain.supplier;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.reference.Reference;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "suplier_agreement")
@Setter
@Getter
@ToString
public class SupplierAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long supplierAgreementID;

    @NotBlank
    @NotNull
    @Size(max = 50)
    @ManyToOne
    @JoinColumn
    private Supplier supplier;

    @OneToMany
    @JoinTable(
            name = "reference_supplier_agreement",
            joinColumns = @JoinColumn(name = "supplierAgreementID"),
            inverseJoinColumns = @JoinColumn(name = "referenceID"))
    private Set<Reference> references;

}
