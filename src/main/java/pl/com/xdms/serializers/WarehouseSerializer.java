package pl.com.xdms.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.com.xdms.domain.warehouse.Warehouse;

import java.io.IOException;

/**
 * Created on 22.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class WarehouseSerializer extends StdSerializer<Warehouse> {
    public WarehouseSerializer() {
        this(null);
    }

    public WarehouseSerializer(Class<Warehouse> w) {
        super(w);
    }

    @Override
    public void serialize(Warehouse warehouse, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Warehouse warehouseSerialized = new Warehouse();
        warehouseSerialized.setWarehouseID(warehouse.getWarehouseID());
        warehouseSerialized.setUrlCode(warehouse.getUrlCode());
        jsonGenerator.writeObject(warehouseSerialized);
    }
}
