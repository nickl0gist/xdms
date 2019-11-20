package pl.com.xdms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.repository.SupplierRepository;

import java.util.List;
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

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> getSuppliersWhereIsActive(Boolean isActive) {
        return supplierRepository.findAllByIsActiveEquals(isActive);
    }

    public List<Supplier> getAllSuppliersOrderedBy(String orderBy, String direction) {
        switch (orderBy){
            case"vendor_code":
                return "asc".equals(direction)
                        ? supplierRepository.findAllByOrderByVendorCodeAsc()
                        : supplierRepository.findAllByOrderByVendorCodeDesc();
            case"name":
                return "asc".equals(direction)
                        ? supplierRepository.findAllByOrderByNameAsc()
                        : supplierRepository.findAllByOrderByNameDesc();
            case"country":
                return "asc".equals(direction)
                        ? supplierRepository.findAllByOrderByCountryAsc()
                        : supplierRepository.findAllByOrderByCountryDesc();
            case"post_code":
                return "asc".equals(direction)
                        ? supplierRepository.findAllByOrderByPostCodeAsc()
                        : supplierRepository.findAllByOrderByPostCodeDesc();
            case"street":
                return "asc".equals(direction)
                        ? supplierRepository.findAllByOrderByStreetAsc()
                        : supplierRepository.findAllByOrderByStreetDesc();
            default: return getAllSuppliers();
        }
    }

    public List<Supplier> search(String searchString) {
        return supplierRepository.findSupplierInSearch(searchString);
    }

    public void save(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    public void save(List<Supplier> supplierList) {
        supplierRepository.saveAll(supplierList);
    }

    public Supplier updateSupplier(Supplier supplier) {
        Optional<Supplier> supplierOptional = supplierRepository.findById(supplier.getSupplierID());
        if(supplierOptional.isPresent()){
            supplierRepository.save(supplier);
        }
        return supplierRepository.findById(supplier.getSupplierID()).orElse(null);
    }
}
