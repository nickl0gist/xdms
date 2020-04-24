package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TpaDaysSetting;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface TPARepository extends JpaRepository<TPA, Long> {

    @Query(nativeQuery = true)
    Optional<TPA> findByTpaNameAndTpaETDPlan(String tpaName, String tpaETDPlan);

    List<TPA> findByTpaDaysSettingInAndDeparturePlanStartsWith(Set<TpaDaysSetting> tpaDaysSettingSet, String tpaDepartureDatePlan);

}
