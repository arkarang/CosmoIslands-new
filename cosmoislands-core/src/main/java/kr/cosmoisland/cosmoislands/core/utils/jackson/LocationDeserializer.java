package kr.cosmoisland.cosmoislands.core.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import kr.cosmoisland.cosmoislands.api.AbstractLocation;

import java.io.IOException;

public class LocationDeserializer extends StdDeserializer<AbstractLocation> {
    protected LocationDeserializer(Class<?> vc) {
        super(vc);
    }

    //{x:(int), y:(int), z:(int), yaw:(float), pitch:(float)}

    @Override
    public AbstractLocation deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
        JsonNode node = parser.getCodec().readTree(parser);
        double x = node.get("x").doubleValue();
        double y = node.get("y").doubleValue();
        double z = node.get("z").doubleValue();
        float yaw = node.get("yaw").floatValue();
        float pitch =node.get("pitch").floatValue();
        return new AbstractLocation(x, y, z, yaw, pitch);
    }
}
