package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.tpa.WorkingDay;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface WorkingDayRepository extends JpaRepository<WorkingDay, Long> {

}
