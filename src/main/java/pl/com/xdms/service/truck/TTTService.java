package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.repository.TTTRepository;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
public class TTTService {
    private final TTTRepository tttRepository;

    @Autowired
    public TTTService(TTTRepository tttRepository) {
        this.tttRepository = tttRepository;
    }
}
