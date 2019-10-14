package pl.com.xdms.domain.trucktimetable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.user.Role;
import pl.com.xdms.domain.warehouse.Warehouse;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "ttt")
@Setter
@Getter
@ToString
public class TruckTimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long tttID;

    @NotNull
    @NotBlank
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
    private Set<Manifest> manifests;

}
