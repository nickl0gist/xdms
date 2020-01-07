package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TTTStatus;
import pl.com.xdms.repository.TTTRepository;
import pl.com.xdms.repository.TttStatusRepository;

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
    public TTTService(TTTRepository tttRepository,TttStatusRepository tttStatusRepository) {
        this.tttRepository = tttRepository;
        this.tttStatusRepository = tttStatusRepository;
    }

    public TTTStatus getTttStatusByEnum (TTTEnum tttEnum){
        Optional<TTTStatus> tttStatusOptional = tttStatusRepository.findByTttStatusName(tttEnum);
        return tttStatusOptional.orElse(null);
    }
}
