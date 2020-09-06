package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;

import java.util.List;
import java.util.Optional;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface TTTRepository extends JpaRepository <TruckTimeTable, Long> {

    @Query(nativeQuery = true)
    Optional<TruckTimeTable> findByTruckNameAndTttETAPlan(String truckName, String tttArrivalDatePlan);

    List<TruckTimeTable> findByWarehouseAndTttArrivalDatePlanStartsWith(Warehouse warehouse, String tttArrivalDatePlan);

    Optional<TruckTimeTable> findByTttIDAndWarehouse(Long tttId, Warehouse warehouse);
}
