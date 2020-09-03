package pl.com.xdms.controller.admin;

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

/**
 * Created on 02.09.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("admin/stor_loc")
public class AdminStorageLocationController {

    private StorageLocationService storageLocationService;
    private RequestErrorService requestErrorService;

    @Autowired
    public AdminStorageLocationController(StorageLocationService storageLocationService, RequestErrorService requestErrorService) {
        this.storageLocationService = storageLocationService;
        this.requestErrorService = requestErrorService;
    }

    /**
     * Endpoint is for updating Storage_Location given by user.
     * @param storageLocation - Storage_Location entity given by user.
     * @param bindingResult - is for checking conditions.
     * @return Status 200 Storage_Location was updated,
     * Status 422 - if any conditions were violated,
     * Status 404 - if given Id was npt found.
     */
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

    /**
     * Endpoint is for creating new Storage_Location in DB.
     * @param storageLocation - Storage_Location entity given by user.
     * @param bindingResult - is for checking conditions.
     * @return Status 201 Storage_Location was successfully saved.
     * Status 422 - if any conditions were violated.
     */
    @PostMapping
    public ResponseEntity<StorageLocation> createStorageLocation(@RequestBody @Valid StorageLocation storageLocation, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(storageLocation);
        }
        log.info("Try to create storage Location {}",storageLocation);
        storageLocationService.save(storageLocation);
        return ResponseEntity.status(201).build();
    }
}
