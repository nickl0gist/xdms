package pl.com.xdms.domain.dto;

import lombok.Data;
import lombok.ToString;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.WarehouseManifest;

import java.util.List;

/**
 * Created on 27.10.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Data
@ToString
public class TttWarehouseManifestDTO {
    private TruckTimeTable ttt;
    private List<WarehouseManifest> warehouseManifestList;

    public TttWarehouseManifestDTO(TruckTimeTable ttt, List<WarehouseManifest> warehouseManifestList) {
        this.ttt = ttt;
        this.warehouseManifestList = warehouseManifestList;
    }
}
