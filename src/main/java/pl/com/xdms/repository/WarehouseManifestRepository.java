package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WarehouseManifest;

import java.util.List;
import java.util.Optional;

/**
 * Created on 13.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface WarehouseManifestRepository extends JpaRepository<WarehouseManifest, Long> {
    List<WarehouseManifest> findAllByTtt(TruckTimeTable ttt);

    void deleteAllByTtt(TruckTimeTable ttt);

    List<WarehouseManifest> findAllByWarehouseAndTtt(Warehouse warehouse, TruckTimeTable truckTimeTable);

    List<WarehouseManifest> findAllByTpa(TPA tpa);

    void deleteByTttAndManifest(TruckTimeTable truckTimeTable, Manifest manifest);

    Optional<WarehouseManifest> findByTttAndManifest(TruckTimeTable ttt, Manifest manifest);

    Optional<WarehouseManifest> findByWarehouseAndManifest(Warehouse warehouse, Manifest manifest);
}


