package pl.com.xdms.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;

import java.io.IOException;

/**
 * Created on 22.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class WarehouseManifestTttSerializer extends StdSerializer<TruckTimeTable> {

    public WarehouseManifestTttSerializer() {
        this(null);
    }

    public WarehouseManifestTttSerializer(Class<TruckTimeTable> ttt) {
        super(ttt);
    }

    @Override
    public void serialize(TruckTimeTable ttt, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        TruckTimeTable tttSerialized = new TruckTimeTable();

        tttSerialized.setTttID(ttt.getTttID());
        tttSerialized.setTruckName(ttt.getTruckName());
        tttSerialized.setTttArrivalDatePlan(ttt.getTttArrivalDatePlan());
        tttSerialized.setTttArrivalDateReal(ttt.getTttArrivalDateReal());
        tttSerialized.setTttStatus(ttt.getTttStatus());
        tttSerialized.setWarehouse(ttt.getWarehouse());
        jsonGenerator.writeObject(tttSerialized);
    }
}
