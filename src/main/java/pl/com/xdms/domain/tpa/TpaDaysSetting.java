package pl.com.xdms.domain.tpa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.warehouse.WhCustomer;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "tpa_days_settings")
@Setter
@Getter
@ToString
public class TpaDaysSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @ManyToOne
    @JoinColumn(name="workingDaysID", nullable=false)
    private WorkingDay workingDay;

    @NotBlank
    @ManyToOne
    @JoinColumn(name="whCustomerID", nullable=false)
    private WhCustomer whCustomer;

    @NotNull
    @Pattern(regexp = "^([0-1]?\\d|2[0-3])(?::([0-5]?\\d))?(?::([0-5]?\\d))?$")
    @NotEmpty
    @Size(min = 5, max =5)
    private String localTime;

    @NotNull
    @NotEmpty
    @Pattern(regexp = "^P(?!$)(\\d+W)?(\\d+D)?(T(?=\\d)(\\d+H)?(\\d+M)?)?$")
    @Size(min = 8, max = 15)
    @Column(name = "transit_time")
    private String transitTime = "P0DT1H0M";
}
