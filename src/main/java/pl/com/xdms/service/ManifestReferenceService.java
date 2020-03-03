package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.repository.ManifestReferenceRepository;

import java.util.List;

/**
 * Created on 01.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
public class ManifestReferenceService {
    private final ManifestReferenceRepository manifestReferenceRepository;

    @Autowired
    public ManifestReferenceService(ManifestReferenceRepository manifestReferenceRepository) {
        this.manifestReferenceRepository = manifestReferenceRepository;
    }

    public List<ManifestReference> saveAll(List<ManifestReference> manifestReferenceSet) {
        return manifestReferenceRepository.saveAll(manifestReferenceSet);
    }

    public List<ManifestReference> getAllManifestReferences(){
        return manifestReferenceRepository.findAll();
    }
}
