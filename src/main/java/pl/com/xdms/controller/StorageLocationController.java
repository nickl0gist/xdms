package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.service.StorageLocationService;

import java.util.List;

/**
 * Created on 12.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/stor_loc")
public class StorageLocationController {
    private StorageLocationService storageLocationService;

    @Autowired
    public StorageLocationController(StorageLocationService storageLocationService) {
        this.storageLocationService = storageLocationService;
    }

    /**
     * Endpoint is for getting List of all Storage Locations from Database
     * @return List\<StorageLocation\>
     */
    @GetMapping
    public List<StorageLocation> getAllStorageLocations(){
        return storageLocationService.getAllStorLocs();
    }

    /**
     * The Endpoint is for getting certain customer from DB
     * @param id - Long Id given by user
     * @return - Status 200 if Storage_Locations was found
     * Status 404 - if Storage_Locations wasn't found
     */
    @GetMapping("/{id}")
    public ResponseEntity getStorageLocationById (@PathVariable Long id){
        StorageLocation storageLocation = storageLocationService.getStorageLocationById(id);
        if (storageLocation != null){
            log.info("Storage Location found {}", storageLocation);
            return ResponseEntity.ok(storageLocation);
        } else {
            log.warn("Storage Location not found, returning error");
            return ResponseEntity.notFound().build();
        }
    }
}
