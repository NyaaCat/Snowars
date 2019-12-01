package cat.nyaa.snowars.config;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.snowars.SnowarsPlugin;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ItemConfigs extends FileConfigure {

    @Serializable
    public String id = "";
    @Serializable
    public String name = "";
    @Serializable
    public List<String> lore = new ArrayList<>();
    @Serializable
    public String behavior = "normal";
    @Serializable
    public Material material = Material.SNOWBALL;


    @Override
    protected String getFileName() {
        return "items.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return SnowarsPlugin.plugin;
    }
}
