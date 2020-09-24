package pl.com.xdms.domain.warehouse;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created on 13.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Embeddable
@Data
@EqualsAndHashCode
public class WarehouseManifestId implements Serializable {

    @Column(name = "wh_id")
    private Long warehouseId;

    @Column(name = "manifest_id")
    private Long manifestId;

    public WarehouseManifestId() {
    }

    public WarehouseManifestId(Long warehouseId, Long manifestId) {
        this.warehouseId = warehouseId;
        this.manifestId = manifestId;
    }
}
