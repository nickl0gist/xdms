package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.StorageLocationService;

import javax.validation.Valid;
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
    private RequestErrorService requestErrorService;

    @Autowired
    public StorageLocationController(StorageLocationService storageLocationService, RequestErrorService requestErrorService) {
        this.storageLocationService = storageLocationService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping
    public List<StorageLocation> getAllStroLocs(){
        return storageLocationService.getAllStorLocs();
    }

    @GetMapping("/{id}")
    public ResponseEntity getStorLocById (@PathVariable Long id){
        StorageLocation storageLocation = storageLocationService.getStorLocById(id);
        if (storageLocation != null){
            log.info("Storage Location found {}", storageLocation);
            return ResponseEntity.ok(storageLocation);
        } else {
            log.warn("Storage Location not found, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    @SuppressWarnings("Duplicates")
    @PutMapping(headers="Accept=application/json")
    public ResponseEntity<StorageLocation> updateStorageLocation(@RequestBody @Valid StorageLocation storageLocation, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(storageLocation);
        }
        StorageLocation storageLocationUpdated = storageLocationService.update(storageLocation);
        return (storageLocationUpdated != null)
                ? ResponseEntity.ok(storageLocationUpdated)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<StorageLocation> addStorLock(@RequestBody @Valid StorageLocation storageLocation, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(storageLocation);
        }
        log.info("Try to create storage Location {}",storageLocation);
        storageLocationService.save(storageLocation);
        return ResponseEntity.status(201).build();
    }

}
