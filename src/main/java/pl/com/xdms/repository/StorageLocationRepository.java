package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.storloc.StorageLocation;

import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
    Optional<StorageLocation> getFirstByCode(String code);
}