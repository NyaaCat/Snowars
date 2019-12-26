package cat.nyaa.snowars;

import cat.nyaa.snowars.event.AutoSpawnTask;
import cat.nyaa.snowars.event.Ticker;
import cat.nyaa.snowars.producer.ProducerManager;
import cat.nyaa.snowars.roller.ItemPoolManager;
import cat.nyaa.snowars.ui.HealthUi;
import cat.nyaa.snowars.utils.RegionManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowarsPlugin extends JavaPlugin {
    public static SnowarsPlugin plugin;
    Commands commands;
    public Configurations configurations;
    Events events;
    I18n i18n;

    public static boolean started = false;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        configurations = new Configurations();
        configurations.reload();
        events = new Events();
        i18n = new I18n(configurations.language);
        commands = new Commands(this, i18n);
        Ticker.getInstance().init();
        HealthUi.getInstance().start();
        ItemPoolManager.getInstance().load();
        ScoreManager.getInstance().load();
        RegionManager.getInstance().load();
        ProducerManager.getInstance().load();
        new BukkitRunnable(){
            @Override
            public void run() {
                ProducerManager.getInstance().save();
                ScoreManager.getInstance().save();
            }
        }.runTaskTimer(this, 5, 100);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
    }

    @Override
    public void onDisable() {
        saveConfig();
        ProducerManager.getInstance().save();
        ScoreManager.getInstance().save();
        ItemPoolManager.getInstance().save();
        RegionManager.getInstance().save();
        super.onDisable();
        plugin = null;
        Ticker.getInstance().stop();
    }

    public void onReload() {
        configurations.reload();
        events.reload();
        ScoreManager.getInstance().load();
        RegionManager.getInstance().load();
        ItemPoolManager.getInstance().load();
        ProducerManager.getInstance().load();
        i18n.reload(configurations.language);
    }
}
