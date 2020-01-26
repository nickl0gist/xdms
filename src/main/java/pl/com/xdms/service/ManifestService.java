package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.repository.ManifestRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created on 01.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
public class ManifestService {

    private final ManifestRepository manifestRepository;

    @Autowired
    public ManifestService(ManifestRepository manifestRepository) {
        this.manifestRepository = manifestRepository;
    }

    public boolean isManifestExisting(Manifest manifest) {
        return findManifest(manifest) != null;
    }

    public Manifest findManifest(Manifest manifest){
        Optional<Manifest> manifestFromDB = manifestRepository.findByManifestCode(manifest.getManifestCode());
        return manifestFromDB.orElse(null);
    }

    public List<Manifest> saveAll(List<Manifest> manifests) {
        return manifestRepository.saveAll(manifests);
    }
}
