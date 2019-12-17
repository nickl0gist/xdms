package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.tpa.TpaStatus;

import java.util.Optional;

/**
 * Created on 13.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface TpaStatusRepository extends JpaRepository<TpaStatus, Long> {
    Optional<TpaStatus> findByStatusName(TPAEnum tpaEnum);
}
