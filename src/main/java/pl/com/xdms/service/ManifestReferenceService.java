package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.repository.ManifestReferenceRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public ManifestReference save(ManifestReference manifestReference){
       return manifestReferenceRepository.save(manifestReference);
    }

    public ManifestReference findById(Long id) {
        return manifestReferenceRepository.findById(id).orElse(null);
    }

    public List<ManifestReference> getAbandonedManifestReferences() {
        return manifestReferenceRepository.findAllByTpaIsNull();
    }

    public List<ManifestReference> reception(List<ManifestReference> manifestReferenceList) {
        return manifestReferenceList.stream().map(this::receipt).collect(Collectors.toList());
    }

    public List<ManifestReference> getManRefListWithinIdSet(Set<Long> ids){
        return manifestReferenceRepository.findAllByManifestReferenceIdIn(ids);
    }

    private ManifestReference receipt(ManifestReference mr) {
        ManifestReference manifestReference = findById(mr.getManifestReferenceId());
        if(manifestReference == null)
            return null;
        manifestReference.setReceptionNumber(mr.getReceptionNumber());
        manifestReference.setDeliveryNumber(mr.getDeliveryNumber());
        manifestReference.setQtyReal(mr.getQtyReal());
        manifestReference.setBoxQtyReal(mr.getBoxQtyReal());
        manifestReference.setGrossWeightReal(mr.getGrossWeightReal());
        manifestReference.setPalletQtyReal(mr.getPalletQtyReal());
        manifestReference.setPalletId(mr.getPalletId());
        manifestReference.setStackability(mr.getStackability());
        log.info("ManifestReference id={} is being Receipted", manifestReference.getManifestReferenceId());
        return save(manifestReference);
    }

    public ManifestReference split(ManifestReference manifestReferenceToSplit, ManifestReference manifestReference) {
        ManifestReference result = new ManifestReference();

        //from manifestReferenceToSplit
        result.setManifest(manifestReferenceToSplit.getManifest());
        result.setStackability(manifestReferenceToSplit.getStackability());
        result.setPalletQtyPlanned(manifestReferenceToSplit.getPalletQtyPlanned());
        result.setQtyPlanned(manifestReferenceToSplit.getQtyPlanned());
        result.setBoxQtyPlanned(manifestReferenceToSplit.getBoxQtyPlanned());
        result.setGrossWeightPlanned(manifestReferenceToSplit.getGrossWeightPlanned());
        result.setPalletHeight(manifestReferenceToSplit.getPalletHeight());
        result.setPalletLength(manifestReferenceToSplit.getPalletLength());
        result.setPalletWidth(manifestReferenceToSplit.getPalletWidth());
        result.setPalletWeight(manifestReferenceToSplit.getPalletWeight());
        result.setReference(manifestReferenceToSplit.getReference());
        result.setReceptionNumber(manifestReferenceToSplit.getReceptionNumber());
        result.setDeliveryNumber(manifestReferenceToSplit.getDeliveryNumber());


        //from manifestReference
        result.setPalletQtyReal(manifestReference.getPalletQtyReal());
        result.setQtyReal(manifestReference.getQtyReal());
        result.setBoxQtyReal(manifestReference.getBoxQtyReal());
        result.setGrossWeightReal(manifestReferenceToSplit.getGrossWeightReal() * result.getQtyReal() / manifestReferenceToSplit.getQtyReal());
        result.setNetWeight(manifestReferenceToSplit.getNetWeight() * result.getQtyReal() / manifestReferenceToSplit.getQtyReal());

        return save(result);
    }

    public ManifestReference update(ManifestReference manifestReferenceToSplit, ManifestReference manifestReferenceToSave) {

        double qtyRealOld = manifestReferenceToSplit.getQtyReal();
        double weightRealOld = manifestReferenceToSplit.getGrossWeightReal();

        manifestReferenceToSplit.setQtyReal(qtyRealOld - manifestReferenceToSave.getQtyReal());
        manifestReferenceToSplit.setBoxQtyReal(manifestReferenceToSplit.getBoxQtyReal() - manifestReferenceToSave.getBoxQtyReal());
        manifestReferenceToSplit.setPalletQtyReal(manifestReferenceToSplit.getPalletQtyReal() - manifestReferenceToSave.getPalletQtyReal());
        manifestReferenceToSplit.setGrossWeightReal(weightRealOld - manifestReferenceToSave.getGrossWeightReal());
        manifestReferenceToSplit.setNetWeight(manifestReferenceToSplit.getNetWeight() - manifestReferenceToSave.getNetWeight());

        return save(manifestReferenceToSplit);
    }
}
