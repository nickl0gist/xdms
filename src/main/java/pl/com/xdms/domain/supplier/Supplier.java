package pl.com.xdms.domain.supplier;

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
@Table(name = "suppliers")
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierID;

    @NotBlank
    @Size(min = 5, max = 150)
    @NotNull
    @Column(unique = true)
    private String name;

    @NotBlank
    @Column(unique = true)
    @NotNull
    @Pattern(regexp = "^[0-9]+")
    private String vendorCode;

    @NotBlank
    @Size(min = 5, max = 30)
    @NotNull
    private String postCode;

    @NotBlank
    @Size(min=2, max = 50)
    @NotNull
    private String country;

    @NotBlank
    @Size(min=2, max = 50)
    @NotNull
    private String city;

    @NotBlank
    @Size(min=2, max = 50)
    @NotNull
    private String street;

    @NotBlank
    @Email
    @Size(max = 200)
    private String email;

    @Column(columnDefinition = "BIT default true", nullable=false)
    private Boolean isActive;

    @OneToMany(mappedBy = "supplier")
    //@JsonManagedReference(value="reference-supplier")
    @JsonIgnore
    private Set<Reference> referenceSet;


/*    @OneToMany(mappedBy = "supplier")
    @JsonManagedReference
    private Set<Manifest> manifests;*/
}
