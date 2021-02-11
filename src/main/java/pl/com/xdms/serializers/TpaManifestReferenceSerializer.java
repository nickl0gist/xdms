package pl.com.xdms.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import pl.com.xdms.domain.manifest.Manifest;

import java.io.IOException;

/**
 * Created on 24.01.2021
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class TpaManifestReferenceSerializer extends StdSerializer<Manifest> {

    public  TpaManifestReferenceSerializer() {
        this(null);
    }

    public TpaManifestReferenceSerializer(Class<Manifest> manifest) {
        super(manifest);
    }

    @Override
    public void serialize(Manifest manifest, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Manifest manifestSerialized = new Manifest();

        manifestSerialized.setManifestCode(manifest.getManifestCode());

        jsonGenerator.writeObject(manifestSerialized);
    }
}
