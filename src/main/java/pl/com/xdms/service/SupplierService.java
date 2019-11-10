package pl.com.xdms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.repository.SupplierRepository;

import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier getSupplierByName(String name){
        Optional<Supplier> supplierOptional = supplierRepository.findByName(name);
        return supplierOptional.orElse(null);
    }

    public Supplier getSupplierById(Long id) {
        Optional<Supplier> supplierOptional = supplierRepository.findById(id);
        return supplierOptional.orElse(null);
    }
}
