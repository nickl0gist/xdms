package pl.com.xdms.domain.customer;

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
@Table(name = "customer_agreement")
@Setter
@Getter
@ToString
public class CustomerAgreement {

    @Id
    @NotBlank
    @NotNull
    @Size(max = 30)
    private String customerAgreementID;

    @NotBlank
    @NotNull
    @Size(max = 50)
    @ManyToOne
    @JoinColumn
    private Customer customer;

    @ManyToOne
    @JoinTable(
            name = "reference_customer_agreement",
            joinColumns = @JoinColumn(name = "customerAgreementID"),
            inverseJoinColumns = @JoinColumn(name = "referenceID"))
    @JsonBackReference
    private Reference references;
}
