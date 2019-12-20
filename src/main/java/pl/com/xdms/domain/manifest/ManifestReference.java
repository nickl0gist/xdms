package pl.com.xdms.domain.manifest;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.tpa.TPA;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
@Table(name ="manifest_reference")
@Setter
@Getter
@ToString
public class ManifestReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manifestReferenceId;

    @Min(1)
    private double qtyPlanned;

    @Min(0)
    @Column(columnDefinition = "int default 0")
    private double qtyReal;

    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int palletQtyPlanned;

    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int palletQtyReal;

    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int boxQtyPlanned;

    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int boxQtyReal;

    @Min(0)
    private double grossWeightPlanned;

    @Min(0)
    @Column(columnDefinition = "int default 0")
    private double grossWeightReal;

    @Min(0)
    double palletHeight;

    @Min(0)
    double palletLength;

    @Min(0)
    double palletWidth;

    @Min(0)
    double palletWeight;

    @Min(0)
    @Column(columnDefinition = "double default 0", name = "net_weight_real")
    private double netWeight;

    private String palletId;

    @Min(1)
    @Column(columnDefinition = "int default 1")
    private int stackability;

    @ManyToOne
    @JoinColumn(name="manifestID", nullable=false)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "manifestCode"
    )
    private Manifest manifest;

    @ManyToOne
    @JoinColumn(name="referenceID", nullable=false)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "supplierAgreement"
    )
    private Reference reference;

    @ManyToOne
    @JoinTable(
            name = "tpa_manifest_reference",
            joinColumns = @JoinColumn(name = "manifest_reference_id"),
            inverseJoinColumns = @JoinColumn(name = "tpaID"))
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "name"
    )
    private TPA tpa;

}