package pl.com.xdms.domain.warehouse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Objects;

@Entity
@Table(name = "warehouses")
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @Pattern(regexp = "^[a-z_]{5,8}$")
    private String urlCode;

    @NotNull
    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "GMT[+-][0-9]{2}")
    private String timeZone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warehouse)) return false;
        Warehouse warehouse = (Warehouse) o;
        return getWarehouseID().equals(warehouse.getWarehouseID()) &&
                getName().equals(warehouse.getName()) &&
                getPostCode().equals(warehouse.getPostCode()) &&
                getCountry().equals(warehouse.getCountry()) &&
                getCity().equals(warehouse.getCity()) &&
                getStreet().equals(warehouse.getStreet()) &&
                getEmail().equals(warehouse.getEmail()) &&
                getIsActive().equals(warehouse.getIsActive()) &&
                getWhType().equals(warehouse.getWhType()) &&
                getUrlCode().equals(warehouse.getUrlCode()) &&
                getTimeZone().equals(warehouse.getTimeZone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWarehouseID(), getName(), getPostCode(), getCountry(), getCity(), getStreet(), getEmail(), getIsActive(), getWhType(), getUrlCode(), getTimeZone());
    }
}
