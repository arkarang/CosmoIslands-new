package kr.cosmoisland.cosmoislands.core.utils.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import kr.cosmoisland.cosmoislands.api.AbstractLocation;

public class JacksonBuilder {

    public static ObjectMapper build(){
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(AbstractLocation.class, new LocationSerializer(AbstractLocation.class));
        module.addDeserializer(AbstractLocation.class, new LocationDeserializer(AbstractLocation.class));
        mapper.registerModule(module);
        return mapper;
    }
}
