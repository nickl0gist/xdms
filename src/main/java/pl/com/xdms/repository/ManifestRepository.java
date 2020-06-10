package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.manifest.Manifest;

import java.util.List;
import java.util.Optional;

/**
 * Created on 01.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface ManifestRepository extends JpaRepository<Manifest, Long> {

    Optional<Manifest> findByManifestCode(String manifestCode);

    List<Manifest> findAllByTruckTimeTableSetIsNull();
}
