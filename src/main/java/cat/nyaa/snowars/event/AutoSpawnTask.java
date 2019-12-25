package cat.nyaa.snowars.event;

import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.ProducerConfig;
import cat.nyaa.snowars.config.RegionConfig;
import cat.nyaa.snowars.producer.BonusSocks;
import cat.nyaa.snowars.producer.ProducerManager;
import cat.nyaa.snowars.utils.RegionManager;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;

public class AutoSpawnTask extends BukkitRunnable {
    static AutoSpawnTask task = null;

    public static void start() {
        if (task != null){
            stop();
        }
        task = new AutoSpawnTask();
        SnowarsPlugin plugin = SnowarsPlugin.plugin;
        task.runTaskTimer(plugin, 5, plugin.configurations.bonusSocksInterval);
    }

    public static void stop() {
        if (task != null) {
            if (!task.isCancelled()) {
                task.cancel();
                task = null;
            }
        }
    }

    @Override
    public void run() {
        Collection<RegionConfig> regions = RegionManager.getInstance().getRegions();
        regions.forEach(regionConfig -> {
            List<String> autoSpawns = regionConfig.autoSpawns;
            autoSpawns.stream().forEach(s -> {
                ProducerConfig config = ProducerManager.getInstance().getConfig(s);
                if (config != null){
                    for (int i = 0; i < 20; i++) {
                        Location location = Utils.randomLocation(regionConfig.region);
                        if (location!=null){
                            BonusSocks bonusSocks = ProducerManager.getInstance().summonBonusSocks(location, config);
                            break;
                        }
                    }
                }
            });
        });
    }
}
