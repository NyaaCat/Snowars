package cat.nyaa.snowars.event;

import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.ui.HealthUi;
import cat.nyaa.snowars.utils.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CheckTeamRegionTask extends BukkitRunnable {
    @Override
    public void run() {
        SnowarsPlugin.plugin.getServer().getWorlds().forEach(world -> {
            List<Player> players = world.getPlayers();
            players.forEach(player ->{
                Location location = player.getLocation();
                List<String> regionsForLocation = RegionManager.getInstance().getRegionsForLocation(location);
                regionsForLocation.forEach(regionConfig -> {
                    if (RegionManager.getInstance().isInvalidRegion(player, regionConfig)) {
                        HealthUi.getInstance().damage(player, 100);
                    }
                });
            });
        });
    }
}
