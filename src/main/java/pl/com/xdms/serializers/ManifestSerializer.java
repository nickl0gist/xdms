package pl.com.xdms.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.supplier.Supplier;

import java.io.IOException;

/**
 * Created on 22.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class ManifestSerializer extends StdSerializer<Manifest> {

    public ManifestSerializer(){ this(null); }

    public ManifestSerializer(Class<Manifest> m) {
        super(m);
    }

    @Override
    public void serialize(Manifest manifest, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Manifest manifestSerialized = new Manifest();
        manifestSerialized.setManifestID(manifest.getManifestID());
        manifestSerialized.setManifestCode(manifest.getManifestCode());
        manifestSerialized.setPalletQtyPlanned(manifest.getPalletQtyPlanned());
        manifestSerialized.setBoxQtyPlanned(manifest.getBoxQtyPlanned());
        manifestSerialized.setTotalWeightPlanned(manifest.getTotalWeightPlanned());
        manifestSerialized.setTotalLdmPlanned(manifest.getTotalLdmPlanned());
        manifestSerialized.setPalletQtyReal(manifest.getPalletQtyReal());
        manifestSerialized.setBoxQtyReal(manifest.getBoxQtyReal());
        manifestSerialized.setTotalWeightReal(manifest.getTotalWeightReal());
        manifestSerialized.setTotalLdmReal(manifest.getTotalLdmReal());
        manifestSerialized.setManifestsReferenceSet(manifest.getManifestsReferenceSet());
        manifestSerialized.setTruckTimeTableSet(manifest.getTruckTimeTableSet());
        manifestSerialized.setTpaSet(manifest.getTpaSet());

        Supplier supplier = new Supplier();
        supplier.setSupplierID(manifest.getSupplier().getSupplierID());
        supplier.setName(manifest.getSupplier().getName());
        manifestSerialized.setSupplier(supplier);

        Customer customer = new Customer();
        customer.setName(manifest.getCustomer().getName());
        customer.setCustomerID(manifest.getCustomer().getCustomerID());
        manifestSerialized.setCustomer(customer);

        jsonGenerator.writeObject(manifestSerialized);
    }
}
