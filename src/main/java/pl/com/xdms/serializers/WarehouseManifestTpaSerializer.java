package pl.com.xdms.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TpaDaysSetting;

import java.io.IOException;

/**
 * Created on 22.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class WarehouseManifestTpaSerializer extends StdSerializer<TPA> {

    public WarehouseManifestTpaSerializer() {
        this(null);
    }

    public WarehouseManifestTpaSerializer(Class<TPA> tpa) {
        super(tpa);
    }

    @Override
    public void serialize(TPA tpa, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        TPA tpaSerialized = new TPA();

        tpaSerialized.setTpaID(tpa.getTpaID());
        tpaSerialized.setName(tpa.getName());
        tpaSerialized.setDeparturePlan(tpa.getDeparturePlan());
        tpaSerialized.setDepartureReal(tpa.getDepartureReal());
        tpaSerialized.setStatus(tpa.getStatus());

        TpaDaysSetting tpaDaysSetting = new TpaDaysSetting();
        tpaDaysSetting.setTransitTime(tpa.getTpaDaysSetting().getTransitTime());

        tpaSerialized.setTpaDaysSetting(tpaDaysSetting);

        jsonGenerator.writeObject(tpaSerialized);
    }
}
