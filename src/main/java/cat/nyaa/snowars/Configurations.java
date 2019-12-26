package cat.nyaa.snowars;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.snowars.config.ItemConfigs;
import cat.nyaa.snowars.item.ItemManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Configurations extends PluginConfigure {

    @Serializable
    public String language = "en_US";

    @Serializable
    public Map<String, ItemConfigs> itemConfigs = new HashMap<>();

    @Serializable
    public int bonusSocksInterval = 400;

    @Serializable
    public double magnification = 1;

    @Override
    protected JavaPlugin getPlugin() {
        return SnowarsPlugin.plugin;
    }

    public void reload(){
        this.load();
        ItemManager.load(this);
    }
}
