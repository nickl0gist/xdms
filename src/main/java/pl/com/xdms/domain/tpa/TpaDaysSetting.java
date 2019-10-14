package pl.com.xdms.domain.tpa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.warehouse.WhCustomer;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tpa_days_settings")
@Setter
@Getter
@ToString
public class TpaDaysSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @ManyToOne
    @JoinColumn(name="workingDaysID", nullable=false)
    private WorkingDays workingDays;

    @NotBlank
    @ManyToOne
    @JoinColumn(name="whCustomerID", nullable=false)
    private WhCustomer whCustomer;

    @NotNull
    private LocalTime localTime;
}
