package pl.com.xdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.repository.ReferenceRepository;

import java.util.List;
import java.util.Optional;

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

    public Reference getRefById(Long id) {
        Optional<Reference> refOpt = referenceRepository.findById(id);
        if (refOpt.isPresent()) {
            return refOpt.get();
        } else {
            return null;
        }
    }

    public Reference updateReference(Reference reference) {
        Optional<Reference> referenceToUpdate = referenceRepository.findById(reference.getReferenceID());
        if (referenceToUpdate.isPresent()) {
            referenceRepository.save(reference);
        } else {
            return null;
        }
        return referenceRepository.findById(reference.getReferenceID()).get();
    }

    public void save(Reference reference) {
        referenceRepository.save(reference);
    }

    public List<Reference> search(String searchString) {
        return referenceRepository.findReferenceInSearch(searchString);
    }

    public List<Reference> getAllReferences(String orderBy, String direction) {
        switch (orderBy) {
            case "number":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByNumberAsc()
                        : referenceRepository.findAllByOrderByNumberDesc();
            case "name":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByNameAsc()
                        : referenceRepository.findAllByOrderByNameDesc();
            case "hscode":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByHsCodeAsc()
                        : referenceRepository.findAllByOrderByHsCodeDesc();
            case "sname":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderBySupplierAsc()
                        : referenceRepository.findAllByOrderBySupplierDesc();
            case "cname":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByCustomerAsc()
                        : referenceRepository.findAllByOrderByCustomerDesc();
                default:
                    return  referenceRepository.findAll();
        }
    }
}
