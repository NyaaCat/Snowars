package cat.nyaa.snowars.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class ProducerConfig extends FileConfigure {
    @Override
    protected String getFileName() {
        return "producer.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return null;
    }
}
