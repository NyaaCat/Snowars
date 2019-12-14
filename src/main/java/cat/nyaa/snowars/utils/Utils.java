package cat.nyaa.snowars.utils;

import cat.nyaa.snowars.SnowarsPlugin;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Utils {
    static final Random random = new Random();
    public static final Vector x_axis = new Vector(1,0,0);
    public static final Vector y_axis = new Vector(0,1,0);
    public static final Vector z_axis = new Vector(0,0,1);


    public static Vector cone(Vector direction, double cone){
        double phi = Utils.random() * 360;
        double theta = Utils.random() * cone;
        Vector clone = direction.clone();
        Vector crossP;

        if (clone.length() == 0) return direction;

        if (clone.getX() != 0 && clone.getZ() != 0) {
            crossP = clone.getCrossProduct(y_axis);
        } else if (clone.getX() != 0 && clone.getY() != 0) {
            crossP = clone.getCrossProduct(z_axis);
        } else {
            crossP = clone.getCrossProduct(x_axis);
        }
        crossP.normalize();

        clone.add(crossP.multiply(Math.tan(Math.toRadians(theta))));
        clone.rotateAroundAxis(direction, Math.toRadians(phi));
        return clone;
    }

    public static double random() {
        return random.nextDouble();
    }

    public static void removeLater(Entity splitedSnowball, int delay) {
        new BukkitRunnable(){
            @Override
            public void run() {
                if (!splitedSnowball.isDead()){
                    splitedSnowball.remove();
                }
            }
        }.runTaskLater(SnowarsPlugin.plugin, delay);
    }


    private static Cache<String, Optional<Team>> teamCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS)
                    .build();

    public static Team getTeam(Entity from) {
        Scoreboard mainScoreboard = from.getServer().getScoreboardManager().getMainScoreboard();
        return getTeam(mainScoreboard, from);
    }

    public static Team getTeam(Scoreboard mainScoreboard, Entity from) {
        String fromEntry = from.getUniqueId().toString();
        if (from instanceof OfflinePlayer){
            fromEntry = from.getName();
        }
        String finalFromEntry = fromEntry;
        try {
            return teamCache.get(finalFromEntry, () -> Optional.ofNullable(mainScoreboard.getEntryTeam(finalFromEntry))).orElse(null);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clearTeamCache(){
        teamCache.cleanUp();
    }

    public static void teleportHome(Entity hit, World world){
        if (!(hit instanceof Player))return;
        Location originLocation = hit.getLocation();
        Location bedSpawnLocation = ((Player) hit).getBedSpawnLocation();
        if (bedSpawnLocation == null){
            bedSpawnLocation = hit.getWorld().getSpawnLocation();
        }
        hit.teleport(bedSpawnLocation);
        World world1 = bedSpawnLocation.getWorld();
        if (world1!=null){
            Location finalBedSpawnLocation = bedSpawnLocation;
            new BukkitRunnable(){
                @Override
                public void run() {
                    world1.playSound(finalBedSpawnLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
                    world1.spawnParticle(Particle.PORTAL, finalBedSpawnLocation, 50, 0, 0, 0, 1);
                }
            }.runTaskLater(SnowarsPlugin.plugin, 1);
        }
        world.playSound(originLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
        world.spawnParticle(Particle.PORTAL, originLocation, 50, 0, 0, 0, 1);
    }

}
