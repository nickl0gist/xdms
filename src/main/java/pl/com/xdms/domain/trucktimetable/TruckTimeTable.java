package pl.com.xdms.domain.trucktimetable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.warehouse.Warehouse;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "ttt")
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
    private LocalDateTime tttArrivalDatePlan;

    private LocalDateTime tttArrivalDateReal;

    @NotBlank
    @NotNull
    @ManyToOne
    @JoinColumn
    private TTTStatus tttStatus;

    @NotBlank
    @NotNull
    @ManyToOne
    @JoinColumn
    private Warehouse warehouse;

    @OneToMany
    @JoinTable(
            name = "ttt_manifest",
            joinColumns = @JoinColumn(name = "tttID"),
            inverseJoinColumns = @JoinColumn(name = "manifestID"))
    @JsonManagedReference
    @ToString.Exclude
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Manifest> manifests;

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
