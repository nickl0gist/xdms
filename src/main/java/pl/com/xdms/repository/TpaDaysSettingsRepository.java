package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.warehouse.WhCustomer;

import java.util.List;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface TpaDaysSettingsRepository extends JpaRepository<TpaDaysSetting, Long> {

    List<TpaDaysSetting> findAllByWhCustomerAndWorkingDay(WhCustomer whCustomer, WorkingDay workingDay);

}