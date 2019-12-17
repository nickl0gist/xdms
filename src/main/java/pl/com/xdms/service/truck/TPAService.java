package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.tpa.TpaStatus;
import pl.com.xdms.repository.TPARepository;
import pl.com.xdms.repository.TpaStatusRepository;

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


}
