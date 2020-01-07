package pl.com.xdms.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created on 27.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class ManifestTttSerializer extends StdSerializer<Set<TruckTimeTable>> {

    public ManifestTttSerializer() {
        this(null);
    }

    public ManifestTttSerializer(Class<Set<TruckTimeTable>> t) {
        super(t);
    }

    @Override
    public void serialize(Set<TruckTimeTable> truckTimeTables, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Set<TruckTimeTable> truckTimeTableResultSet = new LinkedHashSet<>();
        for (TruckTimeTable ttt : truckTimeTables) {
            TruckTimeTable ttt1 = new TruckTimeTable();
            ttt1.setTruckName(ttt.getTruckName());
            ttt1.setTttArrivalDatePlan(ttt.getTttArrivalDatePlan());
            truckTimeTableResultSet.add(ttt1);
        }
        jsonGenerator.writeObject(truckTimeTableResultSet);
    }
}
