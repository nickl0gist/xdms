package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.repository.WorkingDayRepository;

import java.util.List;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
public class WorkingDayService {
    private final WorkingDayRepository workingDayRepository;

    @Autowired
    public WorkingDayService(WorkingDayRepository workingDayRepository) {
        this.workingDayRepository = workingDayRepository;
    }

    public List<WorkingDay> getAllWorkingDays() {
        return workingDayRepository.findAll();
    }

    public WorkingDay getWorkingDayByNumber(Long id) {
        return workingDayRepository.findById(id).orElse(null);
    }
}
