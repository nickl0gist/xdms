package pl.com.xdms.domain.warehouse;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "warehouses")
@Data
@ToString
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warehouseID;

    @NotBlank
    @NotNull
    @Size(min = 5, max = 200)
    private String name;

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

    @Column(columnDefinition = "BIT default true")
    private Boolean isActive;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "wh_typeid")
    private WHType whType;

    @NotBlank
    @NotNull
    @Size(min=5, max = 8)
    @Column(unique = true)
    private String urlCode;

}
