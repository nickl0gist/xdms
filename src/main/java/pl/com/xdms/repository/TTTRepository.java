package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface TTTRepository extends JpaRepository <TruckTimeTable, Long> {

}
