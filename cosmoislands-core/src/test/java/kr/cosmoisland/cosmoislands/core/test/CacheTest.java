package kr.cosmoisland.cosmoislands.core.test;

import com.google.common.cache.CacheLoader;
import kr.cosmoisland.cosmoislands.core.IslandCache;
import kr.cosmoisland.cosmoislands.core.test.utils.TestUUID;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CacheTest {

    IslandCache<Integer> cache  = new IslandCache<>(new CacheLoader<UUID, Integer>() {
        @Override
        public Integer load(@Nonnull UUID uuid) throws Exception {
            return (int)(Math.random()*100);
        }
    });

    @Test
    public void test() throws ExecutionException {
        int value = cache.get(TestUUID.HoBread_Man);
        Assert.assertSame(value, cache.get(TestUUID.HoBread_Man));
        cache.update(TestUUID.HoBread_Man);
        Assert.assertNotSame(value, cache.get(TestUUID.HoBread_Man));
    }

}
