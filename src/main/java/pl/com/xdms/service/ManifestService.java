package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.repository.ManifestReferenceRepository;

/**
 * Created on 01.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
public class ManifestService {

    private final ManifestReferenceRepository manifestReferenceRepository;

    @Autowired
    public ManifestService(ManifestReferenceRepository manifestReferenceRepository) {
        this.manifestReferenceRepository = manifestReferenceRepository;
    }
}
