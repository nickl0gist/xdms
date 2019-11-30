package pl.com.xdms.domain.manifest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.tpa.TPA;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name ="manifestReferenceReal")
@Setter
@Getter
@ToString
public class ManifestReal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manifestRealID;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int qty;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int palletQty;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int boxQty;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "double default 0")
    double palletWeight;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "double default 0")
    double palletHeight;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "double default 0")
    double palletLength;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "double default 0")
    private double grossWeight;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "double default 0")
    private double netWeight;

    @Column(name = "pallet_id")
    private String palletId;

    @ManyToOne
    @NotNull
    @JoinColumn(name="manifestPlanID", nullable=false)
    private ManifestPlan manifestPlan;

    @OneToMany
    @JoinTable(
            name = "tpa_manifest_real",
            joinColumns = @JoinColumn(name = "manifestRealID"),
            inverseJoinColumns = @JoinColumn(name = "tpaID"))
    private Set<TPA> tpaSet;

}
