package pl.com.xdms.domain.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "customers")
@Setter
@Getter
@ToString
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerID;

    @NotBlank
    @NotNull
    @Size(min = 5, max = 150)
    private String name;

    @NotBlank
    @NotNull
    @Column(unique = true)
    private Long customerCode;

    @NotBlank
    @NotNull
    @Size(min = 5, max = 30)
    private String postCode;

    @NotBlank
    @NotNull
    @Size(min=2, max = 50)
    private String country;

    @NotBlank
    @NotNull
    @Size(min=2, max = 50)
    private String city;

    @NotBlank
    @NotNull
    @Size(min=2, max = 50)
    private String street;

    @NotBlank
    @NotNull
    @Email
    @Size(max = 200)
    private String email;

    @Column(columnDefinition = "BIT default true", nullable=false)
    @NotNull
    private Boolean isActive;

/*    @NotNull
    @OneToMany(mappedBy = "customer")
    private Set<Manifest> manifests;*/

}
