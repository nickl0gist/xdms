package pl.com.xdms.domain.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long customerAgreementID;

    @NotBlank
    @NotNull
    @Size(max = 50)
    @ManyToOne
    @JoinColumn
    private Customer customer;
}
