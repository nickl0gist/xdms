package pl.com.xdms.domain.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.reference.Reference;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Set;

@Entity
@Table(name = "customers")
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerID;

    @NotBlank
    @NotNull
    @Size(min = 5, max = 150)
    @Column(unique = true)
    private String name;

    @NotBlank
    @NotNull
    @Column(unique = true)
    @Pattern(regexp = "^[0-9A-Za-z\\-_]+")
    private String customerCode;

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

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    @ToString.Exclude
    private Set<Reference> referenceSet;

    @NotNull
    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "GMT[+-][0-9]{2}")
    private String timeZone;

}
