package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 19.06.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@Slf4j
@RequestMapping("man_ref")
public class ManifestReferenceController {
    private final ManifestReferenceService manifestReferenceService;
    private final RequestErrorService requestErrorService;
    private final TruckService truckService;
    private final ManifestController manifestController;

    @Autowired
    public ManifestReferenceController(ManifestReferenceService manifestReferenceService, RequestErrorService requestErrorService,
                                       TruckService truckService, ManifestController manifestController) {
        this.manifestReferenceService = manifestReferenceService;
        this.requestErrorService = requestErrorService;
        this.truckService = truckService;
        this.manifestController = manifestController;
    }

    //TODO
    @GetMapping("/abandoned")
    public ResponseEntity<List<ManifestReference>> getAllManifestReferencesWithoutTpa(){
        return null;
    }

    //TODO
    @PutMapping("/move_to_tpa/{tpaId:^\\d+$}")
    public ResponseEntity<ManifestReference> moveToAnotherTpa (@RequestBody @Valid ManifestReference manifestReference, @PathVariable Long tpaId){
      return null;
    }

    
}
