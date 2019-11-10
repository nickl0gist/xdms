package pl.com.xdms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.repository.StorageLocationRepository;

import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class StorageLocationService {
    private final StorageLocationRepository storageLocationRepository;

    @Autowired
    public StorageLocationService(StorageLocationRepository storageLocationRepository) {
        this.storageLocationRepository = storageLocationRepository;
    }

    public StorageLocation getStorageLocationByCode(String code){
        Optional<StorageLocation> storLocOpt = storageLocationRepository.getFirstByCode(code);
        return storLocOpt.orElse(null);
    }

    public StorageLocation getStorageLocationById(Long id) {
        Optional<StorageLocation> storLocOpt = storageLocationRepository.findById(id);
        return storLocOpt.orElse(null);
    }
}
