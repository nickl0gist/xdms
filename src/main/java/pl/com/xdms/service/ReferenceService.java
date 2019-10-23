package pl.com.xdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.repository.ReferenceRepository;

import java.util.List;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class ReferenceService {

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceService.class);
    private ReferenceRepository referenceRepository;

    @Autowired
    public ReferenceService(ReferenceRepository referenceRepository) {
        this.referenceRepository = referenceRepository;
    }

    public List<Reference> getAllReferences() {
        return referenceRepository.findAll();
    }
}
