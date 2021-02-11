package pl.com.xdms.domain.manifest;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.serializers.TpaManifestReferenceSerializer;
import pl.com.xdms.serializers.WarehouseManifestTpaSerializer;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "manifest_reference")
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

    @Pattern(regexp = "^\\d+$")
    private String receptionNumber;

    @Pattern(regexp = "^[0-9A-Za-z\\-_]+")
    private String deliveryNumber;

    @ManyToOne
    @JoinColumn(name = "manifestID", nullable = false)
    @JsonSerialize(using = TpaManifestReferenceSerializer.class)
    @ToString.Exclude
    private Manifest manifest;

    @Transient
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "referenceID", nullable = false)
    @NotNull
    private Reference reference;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "tpa_manifest_reference",
            joinColumns = @JoinColumn(name = "manifest_reference_id"),
            inverseJoinColumns = @JoinColumn(name = "tpaID"))
    @JsonSerialize(using = WarehouseManifestTpaSerializer.class)
    @ToString.Exclude
    private TPA tpa;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String manifestCode;
    //@JsonIdentityInfo

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String tpaCode;
}
