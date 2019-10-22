package pl.com.xdms.domain.supplier;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.reference.Reference;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "suplier_agreement")
@Setter
@Getter
@ToString
public class SupplierAgreement {

    @Id
    @NotBlank
    @NotNull
    @Size(max = 30)
    private String supplierAgreementID;

    @NotBlank
    @NotNull
    @Size(max = 50)
    @ManyToOne
    @JoinColumn
    private Supplier supplier;

    @ManyToOne
    @JoinTable(
            name = "reference_supplier_agreement",
            joinColumns = @JoinColumn(name = "supplierAgreementID"),
            inverseJoinColumns = @JoinColumn(name = "referenceID"))
    @JsonBackReference
    private Reference references;

}
