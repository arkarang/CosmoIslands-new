package kr.cosmoislands.cosmoislands.core.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import kr.cosmoislands.cosmoislands.api.AbstractLocation;

import java.io.IOException;

public class LocationSerializer extends StdSerializer<AbstractLocation> {
    protected LocationSerializer(Class<AbstractLocation> t) {
        super(t);
    }

    @Override
    public void serialize(AbstractLocation loc, JsonGenerator gen, SerializerProvider pv) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x", loc.getX());
        gen.writeNumberField("y", loc.getY());
        gen.writeNumberField("z", loc.getZ());
        gen.writeNumberField("yaw", loc.getYaw());
        gen.writeNumberField("pitch", loc.getPitch());
        gen.writeEndObject();
    }
}
