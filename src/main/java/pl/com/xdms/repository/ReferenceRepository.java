package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.reference.Reference;

import java.util.List;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

public interface ReferenceRepository extends JpaRepository<Reference, Long> {

    @Query(nativeQuery = true)
    List<Reference> findReferenceInSearch(String searchWord);
}
