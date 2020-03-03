package pl.com.xdms.domain.warehouse;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.customer.Customer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name ="warehouse_customer")
@Setter
@Getter
@ToString
public class WhCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long whCustomerID;

    @ManyToOne
    @JoinColumn(name="customerID", nullable=false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name="warehouseID", nullable=false)
    private Warehouse warehouse;

    @Column(columnDefinition = "BIT default true", nullable=false)
    private Boolean isActive;

    @NotNull
    @Pattern(regexp = "^P(?!$)(\\d+W)?(\\d+D)?(T(?=\\d)(\\d+H)?(\\d+M)?)?$")
    @Size(min = 8, max = 15)
    private String transitTime = "P0DT1H0M";
}
