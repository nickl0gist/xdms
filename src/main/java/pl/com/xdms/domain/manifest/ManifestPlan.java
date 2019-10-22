package pl.com.xdms.domain.manifest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.reference.Reference;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Entity
@Table(name ="manifestReferencePlan")
@Setter
@Getter
@ToString
public class ManifestPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manifestPlanID;

    @NotBlank
    @Min(1)
    private int qty;

    @NotBlank
    @Min(1)
    private int palletQty;

    @NotBlank
    @Min(1)
    double palletWeight;

    @NotBlank
    @Min(0)
    double palletHeight;

    @NotBlank
    @Min(0)
    double palletLength;

    @NotBlank
    @Min(0)
    private double grossWeight;

    @ManyToOne
    @JoinColumn(name="manifestID", nullable=false)
    private Manifest manifest;

    @ManyToOne
    @JoinColumn(name="referenceID", nullable=false)
    @JsonBackReference
    private Reference reference;

    @OneToMany(mappedBy = "manifestPlan")
    @JsonBackReference
    private Set<ManifestReal> manifestsReal;

}
