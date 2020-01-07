package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TTTStatus;

import java.util.Optional;

/**
 * Created on 27.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface TttStatusRepository extends JpaRepository<TTTStatus, Long> {
    Optional<TTTStatus> findByTttStatusName(TTTEnum tpaEnum);
}
