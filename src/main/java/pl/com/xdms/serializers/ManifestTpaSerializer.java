package pl.com.xdms.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.com.xdms.domain.tpa.TPA;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created on 27.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class ManifestTpaSerializer extends StdSerializer<Set<TPA>> {

    public ManifestTpaSerializer() {
        this(null);
    }

    public ManifestTpaSerializer(Class<Set<TPA>> t) {
        super(t);
    }

    @Override
    public void serialize(Set<TPA> tpas, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Set<TPA> tpaSet = new LinkedHashSet<>();
        for (TPA tpa : tpas) {
            TPA tpa1 = new TPA();
            tpa1.setName(tpa.getName());
            tpa1.setDeparturePlan(tpa.getDeparturePlan());
            tpaSet.add(tpa1);
        }
        jsonGenerator.writeObject(tpaSet);
    }
}
