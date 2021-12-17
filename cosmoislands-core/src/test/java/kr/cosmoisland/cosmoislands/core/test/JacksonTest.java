package kr.cosmoisland.cosmoislands.core.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.core.utils.jackson.JacksonBuilder;
import org.junit.Assert;
import org.junit.Test;

public class JacksonTest {

    @Test
    public void test_01_serialization() throws JsonProcessingException {
        ObjectMapper mapper = JacksonBuilder.build();
        AbstractLocation loc = new AbstractLocation(1.0, 2.0, 3.0, 4f, 5f);
        String serialized = mapper.writeValueAsString(loc);
        AbstractLocation loc2 = mapper.readValue(serialized, AbstractLocation.class);
        Assert.assertEquals(1.0d, loc2.getX(), 0d);
        Assert.assertEquals(2.0d, loc2.getY(), 0d);
        Assert.assertEquals(3.0d, loc2.getZ(), 0d);
        Assert.assertEquals(4f, loc2.getYaw(), 0d);
        Assert.assertEquals(5f, loc2.getPitch(), 0d);

    }
}
