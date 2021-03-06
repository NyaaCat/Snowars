package cat.nyaa.snowars.item;

import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.event.DelayedTriggerEvent;
import cat.nyaa.snowars.event.TickEvent;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.event.Ticker;
import cat.nyaa.snowars.item.impl.SnowballNormal;
import cat.nyaa.snowars.utils.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class AbstractSnowball implements SnowballHandler {
    private static SnowarsPlugin plugin;
    public static NamespacedKey SNOWBALL_FROM = new NamespacedKey(SnowarsPlugin.plugin, "snowball_data");
    public static NamespacedKey IMMUTABLE_TICK = new NamespacedKey(SnowarsPlugin.plugin, "immutable_tick");
    public static final SnowballNormal NORMAL = new SnowballNormal();

    public AbstractSnowball() {
        super();
        plugin = SnowarsPlugin.plugin;
    }

    public static Projectile launch(SnowballHandler handler, Class<? extends Projectile> type, Entity from, Location fromLocation, Vector towards, double cone, double speed, boolean gravity) {
        towards = Utils.cone(towards, cone);
        towards.normalize().multiply(speed);
        Projectile snowball;

        if (from instanceof LivingEntity && fromLocation == null) {
            snowball = ((LivingEntity) from).launchProjectile(type, towards);
            snowball.setVelocity(towards);
            snowball.setGravity(gravity);
        } else {
            World world = from.getWorld();
            Vector finalTowards = towards;
            snowball = world.spawn(fromLocation, type, snowball1 -> {
                snowball1.setVelocity(finalTowards);
                snowball1.setGravity(gravity);
            });
        }
        SnowballManager.getInstance().register(snowball, handler);
        snowball.getPersistentDataContainer().set(SNOWBALL_FROM, PersistentDataType.STRING, from.getUniqueId().toString());
        handler.onLaunch(from, snowball);

        return snowball;
    }

    public static Projectile launchSnowball(SnowballHandler handler, Entity from, Location fromLocation, Vector towards, double cone, double speed, boolean gravity) {
        return launch(handler, Snowball.class, from, fromLocation, towards, cone, speed, gravity);
    }

    boolean hitCooldown = false;

    @Override
    public void onHitEntity(Entity from, Entity related, Entity hit, ProjectileHitEvent event) {
        if (!(hit instanceof Player) || hit instanceof ArmorStand) return;
        int noDamageTicks = ((LivingEntity) hit).getNoDamageTicks();
        if (noDamageTicks > 0 && noDamageTicks != 20) return;
        Scoreboard mainScoreboard = from.getServer().getScoreboardManager().getMainScoreboard();
        Team fromTeam = Utils.getTeam(mainScoreboard, from);
        Team hitTeam = Utils.getTeam(mainScoreboard, hit);
        double hitScore = getHitScore(from, hit, fromTeam, hitTeam);
        ScoreManager instance = ScoreManager.getInstance();
        instance.addFor(from, hitScore);
        double damageAmplifier = SnowarsPlugin.plugin.configurations.damageAmplifier;
        double damage = getDamage(from, hit, fromTeam, hitTeam);
        ItemStack item = getItem();
        instance.damage(hit, damage * damageAmplifier, from, item);
        if (damage > 0 && !hitCooldown){
            hitCooldown = true;
            World world = from.getWorld();
            Location location = hit.getLocation();
            world.spawnParticle(Particle.SNOW_SHOVEL, location, 20, 0, 0, 0, 0.1, null);
            world.playSound(from.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 0.5f);
            new BukkitRunnable() {
                @Override
                public void run() {
                    hitCooldown = false;
                }
            }.runTaskLater(SnowarsPlugin.plugin, 10);
        }
    }

    public static double zeroOr(Team from, Team hit, double def) {
        return Objects.equals(from, hit) ? 0 : def;
    }

    @Override
    public void registerDelayedEvent(Entity from, Entity related, int delay, BukkitRunnable runnable, Predicate<DelayedTriggerEvent> shouldRun) {
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
    public void registerTickEvent(Entity from, Entity related, TickTask tickTask) {
        Ticker.getInstance().register(tickTask);
    }

    @Override
    public ItemStack getItem() {
        return ItemManager.getInstance().getItem(this);
    }

    public boolean isNormal(){
        return this instanceof SnowballNormal;
    }
}
