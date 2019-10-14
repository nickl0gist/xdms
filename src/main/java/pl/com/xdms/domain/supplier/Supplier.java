package pl.com.xdms.domain.supplier;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.manifest.Manifest;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "suppliers")
@Setter
@Getter
@ToString
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long supplierID;

    @NotBlank
    @Size(min = 5, max = 150)
    @NotNull
    private String name;

    @NotBlank
    @Column(unique = true)
    @NotNull
    private Long vendorCode;

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
    private Set<Manifest> manifests;
}
