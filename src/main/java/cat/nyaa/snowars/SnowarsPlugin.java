package cat.nyaa.snowars;

import org.bukkit.plugin.java.JavaPlugin;

public class SnowarsPlugin extends JavaPlugin {
    public static SnowarsPlugin plugin;
    Commands commands;
    Configurations configurations;
    Events events;
    I18n i18n;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        configurations = new Configurations();
        events = new Events();
        i18n = new I18n(configurations.language);
        commands = new Commands(this, i18n);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        plugin = null;
    }


    public void onReload() {
        configurations.reload();
        events.reload();
        i18n.reload(configurations.language);
    }
}
