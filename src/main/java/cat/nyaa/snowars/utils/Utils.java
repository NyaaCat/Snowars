package cat.nyaa.snowars.utils;

import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.RegionConfig;
import cat.nyaa.snowars.item.SnowballManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Utils {
    static final Random random = new Random();
    public static final Vector x_axis = new Vector(1, 0, 0);
    public static final Vector y_axis = new Vector(0, 1, 0);
    public static final Vector z_axis = new Vector(0, 0, 1);


    public static Vector cone(Vector direction, double cone) {
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
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!splitedSnowball.isDead()) {
                    splitedSnowball.remove();
                    if (splitedSnowball instanceof Projectile) {
                        SnowballManager.getInstance().remove((Projectile) splitedSnowball);
                    }
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
        if (from instanceof OfflinePlayer) {
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

    public static void clearTeamCache() {
        teamCache.cleanUp();
    }

    public static void teleportHome(Entity hit, World world) {
        if (!(hit instanceof Player)) return;
        Location originLocation = hit.getLocation().clone();
        Location bedSpawnLocation = null;
        Team team = Utils.getTeam(hit);
        if (team == null) {
            bedSpawnLocation = ((Player) hit).getBedSpawnLocation();
        } else {
            RegionConfig teamRegion = RegionManager.getInstance().getTeamRegion(team);
            if (teamRegion != null) {
                for (int i = 0; i < 20; i++) {
                    Location location = Utils.randomLocation(teamRegion.region);
                    if (location != null) {
                        bedSpawnLocation = location;
                        break;
                    }
                }
                if (bedSpawnLocation == null) {
                    bedSpawnLocation = ((Player) hit).getBedSpawnLocation();
                }
            }
        }
        if (bedSpawnLocation == null) {
            bedSpawnLocation = hit.getWorld().getSpawnLocation().clone();
        }
        hit.teleport(bedSpawnLocation);
        World world1 = bedSpawnLocation.getWorld();
        if (world1 != null) {
            Location finalBedSpawnLocation = bedSpawnLocation;
            new BukkitRunnable() {
                @Override
                public void run() {
                    world1.playSound(finalBedSpawnLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 5, 1);
                    world1.spawnParticle(Particle.PORTAL, finalBedSpawnLocation, 500, 0, 0, 0, 1);
                }
            }.runTaskLater(SnowarsPlugin.plugin, 1);
        }
        world.playSound(originLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 5, 1);
        world.spawnParticle(Particle.PORTAL, originLocation, 500, 0, 0, 0, 1);
    }

    public static <T> T weightedRandomPick(Map<T, Integer> weightMap) {
        int sum = weightMap.values().stream().mapToInt(Integer::intValue)
                .sum();
        if (sum == 0) {
            return weightMap.keySet().stream().findFirst().orElse(null);
        }
        int selected = random.nextInt(sum);
        Iterator<Map.Entry<T, Integer>> iterator = weightMap.entrySet().stream().iterator();
        int count = 0;
        Map.Entry<T, Integer> next = null;
        while (iterator.hasNext()) {
            next = iterator.next();
            int nextCount = count + next.getValue();
            if (count <= selected && nextCount > selected) {
                break;
            }
            count = nextCount;
        }
        return next == null ? null : next.getKey();
    }

    public static String colored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static void removeNow(Projectile projectile) {
        projectile.remove();
        SnowballManager.getInstance().remove(projectile);
    }

    public static ArmorStand summonNameDisplay(Location add) {
        World world = add.getWorld();
        return world.spawn(add, ArmorStand.class, armorStand -> {
            armorStand.setSmall(true);
            armorStand.setBasePlate(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName("");
            armorStand.setVisible(false);
        });
    }

    public static Location randomLocation(RegionConfig.Region region) {
        World world = SnowarsPlugin.plugin.getServer().getWorld(region.world);
        if (world == null) return null;
        int xRange = region.xMax - region.xMin;
        int yRange = region.yMax - region.yMin;
        int zRange = region.zMax - region.zMin;
        Location selected = null;
        for (int i = 0; i < 20; i++) {
            int dX = (int) (random() * xRange);
            int dy = (int) (random() * (yRange/2));
            int dZ = (int) (random() * zRange);
            Location location = new Location(world, region.xMin + dX, region.yMin + dy, region.zMin + dZ);
            Location validSpawnLocationInY = findValidSpawnLocationInY(location);
            if (validSpawnLocationInY != null) {
                selected = validSpawnLocationInY.clone().add(0.5, 1, 0.5);
                break;
            }
        }
        return selected;
    }

    private static Location findValidSpawnLocationInY(Location targetLocation) {
        for (int j = 0; j > -20; j--) {
            Location clone = targetLocation.clone().add(0, j, 0);
            if (isValidLocation(clone)) {
                return clone;
            }
        }
        for (int j = 0; j < 10; j++) {
            Location clone = targetLocation.clone().add(0, j, 0);
            if (isValidLocation(clone)) {
                return clone;
            }
        }
        return null;
    }

    private static boolean isValidLocation(Location targetLocation) {
        Block block = targetLocation.getBlock();
        Block lowerBlock = block.getRelative(BlockFace.DOWN);
        Block upperBlock = block.getRelative(BlockFace.UP);
        return !block.getType().isSolid() && !upperBlock.getType().isSolid() && ((lowerBlock.getType().isSolid() || block.getType().equals(Material.WATER)));
    }

    public static Team chooseTeam() {
        Scoreboard mainScoreboard = SnowarsPlugin.plugin.getServer().getScoreboardManager().getMainScoreboard();
        Set<Team> teams = mainScoreboard.getTeams();
        Team team = teams.stream().min(Comparator.comparingInt(Team::getSize))
                .orElse(null);
        return team;
    }
}
