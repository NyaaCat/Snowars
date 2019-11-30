package cat.nyaa.snowars;

import org.bukkit.event.Listener;

public class Events implements Listener {

    public Events() {
        SnowarsPlugin plugin = SnowarsPlugin.plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {

    }
}
