package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TTTStatus;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.repository.TTTRepository;
import pl.com.xdms.repository.TttStatusRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
public class TTTService {
    private final TTTRepository tttRepository;
    private final TttStatusRepository tttStatusRepository;

    @Autowired
    public TTTService(TTTRepository tttRepository, TttStatusRepository tttStatusRepository) {
        this.tttRepository = tttRepository;
        this.tttStatusRepository = tttStatusRepository;
    }

    public TTTStatus getTttStatusByEnum(TTTEnum tttEnum) {
        Optional<TTTStatus> tttStatusOptional = tttStatusRepository.findByTttStatusName(tttEnum);
        return tttStatusOptional.orElse(null);
    }

    public boolean isTttExisting(TruckTimeTable ttt) {
        TruckTimeTable tttFromDb = getTttByTruckNameAndTttArrivalDatePlan(ttt);
        return tttFromDb != null;
    }

    public TruckTimeTable getTttByTruckNameAndTttArrivalDatePlan(TruckTimeTable ttt) {
        Optional<TruckTimeTable> tttFromDb = tttRepository.findByTruckNameAndTttETAPlan(ttt.getTruckName(),
                ttt.getTttArrivalDatePlan());
        return tttFromDb.orElse(null);
    }

    public List<TruckTimeTable> saveAll(List<TruckTimeTable> truckTimeTableSet) {
        return tttRepository.saveAll(truckTimeTableSet);
    }

    public List<TruckTimeTable> getAllTtt() {
        return tttRepository.findAll();
    }

    public TruckTimeTable save(TruckTimeTable ttt) {
        return tttRepository.save(ttt);
    }

    public List<TruckTimeTable> getTttByWarehouseAndDay(Warehouse warehouse, String tttArrivalDatePlan){
        return tttRepository.findByWarehouseAndTttArrivalDatePlanStartsWith(warehouse, tttArrivalDatePlan);
    }

    public TruckTimeTable getTttById(Long id) {
        return tttRepository.findById(id).orElse(null);
    }

    public TruckTimeTable getTTTByWarehouseAndId(Long id, Warehouse warehouse){
        return tttRepository.findByTttIDAndWarehouse(id, warehouse).orElse(null);
    }

    public void deleteTtt(TruckTimeTable truckTimeTable) {
        tttRepository.delete(truckTimeTable);
    }

    public void setArrive(TruckTimeTable ttt) {
        String time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();
        ttt.setTttStatus(getTttStatusByEnum(TTTEnum.ARRIVED));
        ttt.setTttArrivalDateReal(time);
        save(ttt);
    }
}
