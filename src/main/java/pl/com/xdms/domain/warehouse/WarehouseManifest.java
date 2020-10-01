package pl.com.xdms.domain.warehouse;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.serializers.ManifestSerializer;
import pl.com.xdms.serializers.WarehouseManifestTpaSerializer;
import pl.com.xdms.serializers.WarehouseManifestTttSerializer;
import pl.com.xdms.serializers.WarehouseSerializer;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created on 13.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Entity
@Table(name = "warehouse_manifest")
@Data
public class WarehouseManifest {

    @EmbeddedId
    private WarehouseManifestId warehouseManifestId;

    @MapsId("wh_id")
    @ManyToOne
    @JoinColumn(name="wh_id")
    @NotNull
    @JsonSerialize(using = WarehouseSerializer.class)
    private Warehouse warehouse;

    @MapsId("manifestid")
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="manifest_id")
    @NotNull
    @JsonSerialize(using = ManifestSerializer.class)
    private Manifest manifest;

    @NotNull
    @ManyToOne
    @JoinColumn(name="ttt_id")
    @JsonSerialize(using = WarehouseManifestTttSerializer.class)
    private TruckTimeTable ttt;

    @ManyToOne
    @JoinColumn(name="tpa_id")
    @JsonSerialize(using = WarehouseManifestTpaSerializer.class)
    private TPA tpa;

    @Min(0)
    private Integer palletQty;

    @Min(0)
    private Integer boxQtyReal;

    @Min(0)
    private Double grossWeight;

    @Min(0)
    private Double netWeightReal;

    @Min(0)
    private Double palletHeight;

    @Min(0)
    private Double palletWidth;

    @Min(0)
    private Double palletLength;

    private Boolean kpiLabel;

    private Boolean kpiDocument;

    private Boolean kpiManifest;

    @Override
    public String toString() {
        String tpaStr = tpa == null ? "" : tpa.getTpaID() + " " + tpa.getName();
        return "WarehouseManifest{" +
                "warehouseManifestId=" + warehouseManifestId +
                ", warehouse=" + warehouse.getWarehouseID() + " " + warehouse.getName() +
                ", manifest=" + manifest.getManifestID() + " " + manifest.getManifestCode() +
                ", ttt=" + ttt.getTttID() + " " + ttt.getTruckName() +
                ", tpa=" + tpaStr +
                ", palletQty=" + palletQty +
                ", boxQtyReal=" + boxQtyReal +
                ", grossWeight=" + grossWeight +
                ", netWeightReal=" + netWeightReal +
                ", palletHeight=" + palletHeight +
                ", palletWidth=" + palletWidth +
                ", palletLength=" + palletLength +
                ", kpiLabel=" + kpiLabel +
                ", kpiDocument=" + kpiDocument +
                ", kpiManifest=" + kpiManifest +
                '}';
    }
}
