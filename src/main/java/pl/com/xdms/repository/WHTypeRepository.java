package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.warehouse.WHType;
import pl.com.xdms.domain.warehouse.WHTypeEnum;

/**
 * Created on 23.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface WHTypeRepository extends JpaRepository<WHType, Long> {
    WHType findWHTypeByType(WHTypeEnum whTypeEnum);
}
