package pl.com.xdms.domain.manifest;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.reference.Reference;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name ="manifest_reference")
@Setter
@Getter
@ToString
public class ManifestReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manifestReferenceId;

    @NotBlank
    @Min(1)
    private int qtyPlanned;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int qtyReal;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int palletQtyPlanned;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int palletQtyReal;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int boxQtyPlanned;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private int boxQtyReal;

    @NotBlank
    @Min(0)
    private double grossWeightPlanned;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "int default 0")
    private double grossWeightReal;

    @NotBlank
    @Min(0)
    double palletHeight;

    @NotBlank
    @Min(0)
    double palletLength;

    @NotBlank
    @Min(0)
    double palletWidth;

    @NotBlank
    @Min(0)
    double palletWeight;

    @NotBlank
    @Min(0)
    @Column(columnDefinition = "double default 0", name = "net_weight_real")
    private double netWeight;

    private String palletId;

    @NotBlank
    @Min(1)
    @Column(columnDefinition = "int default 1")
    private int stackability;

    @ManyToOne
    @JoinColumn(name="manifestID", nullable=false)
    private Manifest manifest;

    @ManyToOne
    @JoinColumn(name="referenceID", nullable=false)
    @JsonBackReference
    private Reference reference;

}
