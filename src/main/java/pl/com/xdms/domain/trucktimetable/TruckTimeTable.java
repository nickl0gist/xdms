package pl.com.xdms.domain.trucktimetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.warehouse.Warehouse;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "ttt", uniqueConstraints = {@UniqueConstraint(columnNames = {"truckName", "tttArrivalDatePlan"})})
@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TruckTimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tttID;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^[0-9A-Za-z\\-_]+")
    private String truckName;

    @NotBlank
    @NotNull
    @Size(min = 16, max = 19)
    @Pattern(regexp = "^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$")
    private String tttArrivalDatePlan;

    @Pattern(regexp = "^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$")
    private String tttArrivalDateReal;

    @ManyToOne
    @JoinColumn
    private TTTStatus tttStatus;

    @NotNull
    @ManyToOne
    @JoinColumn
    private Warehouse warehouse;

    @Transient
    private Boolean isActive;

    @ManyToMany
    @JoinTable(
            name = "ttt_manifest",
            joinColumns = @JoinColumn(name = "tttID"),
            inverseJoinColumns = @JoinColumn(name = "manifestID"))
    @ToString.Exclude
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Manifest> manifestSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TruckTimeTable)) return false;
        TruckTimeTable that = (TruckTimeTable) o;
        return truckName.equals(that.truckName) &&
                tttArrivalDatePlan.equals(that.tttArrivalDatePlan) &&
                warehouse.equals(that.warehouse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(truckName, tttArrivalDatePlan, warehouse);
    }
}
