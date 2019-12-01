package cat.nyaa.snowars.item;

import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.event.DelayedTriggerEvent;
import cat.nyaa.snowars.event.TickEvent;
import cat.nyaa.snowars.event.Ticker;
import cat.nyaa.snowars.utils.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractSnowball implements SnowballHandler {
    private static SnowarsPlugin plugin;
    public static NamespacedKey SNOWBALL_DATA = new NamespacedKey(SnowarsPlugin.plugin, "snowball_data");
    public static NamespacedKey IMMUTABLE_TICK = new NamespacedKey(SnowarsPlugin.plugin, "immutable_tick");

    public AbstractSnowball() {
        super();
        plugin = SnowarsPlugin.plugin;
    }

    public static Projectile launchSnowball(SnowballHandler handler, Entity from, Location fromLocation, Vector towards, double cone, double speed, boolean gravity){
        Class<Snowball> snowballClass = Snowball.class;
        towards = Utils.cone(towards, cone);
        towards.normalize().multiply(speed);
        Snowball snowball;

        if (from instanceof LivingEntity && fromLocation == null) {
            snowball = ((LivingEntity) from).launchProjectile(snowballClass, towards);
            snowball.setVelocity(towards);
            snowball.setGravity(gravity);
        }else {
            World world = from.getWorld();
            Vector finalTowards = towards;
            snowball = world.spawn(fromLocation, snowballClass, snowball1 -> {
                snowball1.setVelocity(finalTowards);
                snowball1.setGravity(gravity);
            });
        }
        snowball.getPersistentDataContainer().set(SNOWBALL_DATA, PersistentDataType.STRING, from.getUniqueId().toString());
        handler.onLaunch(from, snowball);
        return snowball;
    }

    @Override
    public void onHitEntity(Entity from, Entity hit, ProjectileHitEvent event) {
        Scoreboard mainScoreboard = from.getServer().getScoreboardManager().getMainScoreboard();
        Team fromTeam = getTeam(mainScoreboard, from);
        Team hitTeam = getTeam(mainScoreboard, hit);
        int hitScore = getHitScore(from, hit, fromTeam, hitTeam);
        ScoreManager.getInstance().addFor(from, hitScore);
    }

    private static Cache<String, Optional<Team>> teamCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS)
                    .build();

    public Team getTeam(Scoreboard mainScoreboard, Entity from) {
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

    public static void clearCache(){
        teamCache.cleanUp();
    }

    public static int zeroOr(Team from, Team hit, int def){
        return from.equals(hit)? 0 : def;
    }

    @Override
    public void registerDelayedEvent(Entity from, Entity related,int delay, BukkitRunnable runnable, Predicate<DelayedTriggerEvent> shouldRun) {
        new BukkitRunnable() {
            @Override
            public void run() {
                DelayedTriggerEvent delayedTriggerEvent = new DelayedTriggerEvent(from, related, delay);
                plugin.getServer().getPluginManager().callEvent(delayedTriggerEvent);
                if (shouldRun.test(delayedTriggerEvent)) {
                    runnable.runTask(plugin);
                }
            }
        }.runTaskLater(plugin, delay);
    }

    @Override
    public void registerTickEvent(Entity from, Entity related, BukkitRunnable runnable, Predicate<TickEvent> shouldRemove) {
        Ticker.getInstance().register(runnable, shouldRemove);
    }
}
