package cat.nyaa.snowars;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class Configurations extends PluginConfigure {

    @Serializable
    public String language = "en_US";

    @Override
    protected JavaPlugin getPlugin() {
        return SnowarsPlugin.plugin;
    }

    public void reload(){
        this.load();
    }
}
