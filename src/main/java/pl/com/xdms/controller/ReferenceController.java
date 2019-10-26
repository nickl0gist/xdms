package pl.com.xdms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.payload.UploadFileResponse;
import pl.com.xdms.service.CustomerAgreementService;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.ReferenceService;
import pl.com.xdms.service.SupplierAgreementService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@RequestMapping("coordinator/references")
public class ReferenceController {
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceController.class);

    private final ReferenceService referenceService;
    private final SupplierAgreementService supplierAgreementService;
    private final CustomerAgreementService customerAgreementService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ReferenceController(ReferenceService referenceService,
                               SupplierAgreementService supplierAgreementService,
                               CustomerAgreementService customerAgreementService,
                               FileStorageService fileStorageService) {
        this.referenceService = referenceService;
        this.supplierAgreementService = supplierAgreementService;
        this.customerAgreementService = customerAgreementService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<Reference> getAllReferences() {
        return referenceService.getAllReferences();
    }

    @GetMapping({"/{orderBy}/{direction}", "/{orderBy}"})
    public List<Reference> getAllReferences(@PathVariable String orderBy, @PathVariable String direction) {
        return referenceService.getAllReferences(orderBy, direction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reference> getReferenceById(@PathVariable Long id){
        Reference reference = referenceService.getRefById(id);
        if (reference != null){
            LOG.info("Reference found {}", reference);
            return ResponseEntity.ok(reference);
        } else {
            LOG.warn("Reference wasn't found, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Reference> updateReference(@RequestBody Reference reference){
        Reference repositoryReference = referenceService.updateReference(reference);
        return (repositoryReference != null)
                ? ResponseEntity.ok(repositoryReference)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addReference(@RequestBody Reference reference){
        LOG.info("Try to create reference with Id:{} , number:{}",reference.getReferenceID(), reference.getNumber());
        referenceService.save(reference);
    }

    @GetMapping("/search/{searchString}")
    public List<Reference> searchReferencesByString(@PathVariable String searchString){
        return referenceService.search(searchString);
    }

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("coordinator/references/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    //TODO Change to give the file with actual database of references.
    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            LOG.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
