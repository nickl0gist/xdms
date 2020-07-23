package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.manifest.ManifestReference;

import java.util.List;
import java.util.Set;

/**
 * Created on 01.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface ManifestReferenceRepository extends JpaRepository<ManifestReference, Long> {
    List<ManifestReference> findAllByTpaIsNull();

    List<ManifestReference> findAllByManifestReferenceIdIn(Set<Long> ids);
}
