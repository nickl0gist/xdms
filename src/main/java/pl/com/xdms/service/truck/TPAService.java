package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.tpa.TpaStatus;
import pl.com.xdms.repository.TPARepository;
import pl.com.xdms.repository.TpaStatusRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

@Service
@Slf4j
public class TPAService {
    private final TPARepository tpaRepository;
    private final TpaStatusRepository tpaStatusRepository;

    @Autowired
    public TPAService(TPARepository tpaRepository, TpaStatusRepository tpaStatusRepository) {
        this.tpaRepository = tpaRepository;
        this.tpaStatusRepository = tpaStatusRepository;
    }

    public TpaStatus getTpaStatusByEnum (TPAEnum tpaEnum){
        Optional<TpaStatus> tpaStatusOptional = tpaStatusRepository.findByStatusName(tpaEnum);
        return tpaStatusOptional.orElse(null);
    }

    public boolean isTpaExisting(TPA tpa) {
        return getTpaByTpaNameAndTpaETDPlan(tpa) != null;
    }

    private TPA getTpaByTpaNameAndTpaETDPlan(TPA tpa) {
        Optional<TPA> tpaFromDB = tpaRepository.findByTpaNameAndTpaETDPlan(tpa.getName(),
                tpa.getDeparturePlan());
        return tpaFromDB.orElse(null);
    }

    public List<TPA> saveAll(List<TPA> tpaSet) {
        return tpaRepository.saveAll(tpaSet);
    }

    public TPA save(TPA tpa){
        return tpaRepository.save(tpa);
    }

    public List<TPA> getAllTpa() {
        return tpaRepository.findAll();
    }
}
