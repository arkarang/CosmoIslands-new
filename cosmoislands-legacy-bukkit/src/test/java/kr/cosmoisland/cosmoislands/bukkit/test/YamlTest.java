package kr.cosmoisland.cosmoislands.bukkit.test;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class YamlTest {

    @Test
    public void test(){
        YamlConfiguration config = getUTF8Yaml(getResource());
        System.out.println(config.getKeys(true));
    }

    protected File getResource(){
        return new File("src/test/resources/test.yml");
    }

    protected YamlConfiguration getUTF8Yaml(File file) {
        YamlConfiguration config = new YamlConfiguration();

        try {
            FileInputStream fileinputstream = new FileInputStream(file);
            config.load(new InputStreamReader(fileinputstream, StandardCharsets.UTF_8));
        } catch (InvalidConfigurationException | IOException var5) {
            var5.printStackTrace();
        }

        return config;
    }

}
