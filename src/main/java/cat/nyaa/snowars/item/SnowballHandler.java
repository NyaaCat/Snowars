package cat.nyaa.snowars.item;

import cat.nyaa.snowars.event.DelayedTriggerEvent;
import cat.nyaa.snowars.event.TickEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface SnowballHandler {
    boolean onUse(LivingEntity from, PlayerInteractEvent event);
    void onLaunch(Entity from, Snowball event);
    void onHitEntity(Entity from, Entity hit, ProjectileHitEvent event);
    void onHitBlock(Entity from, Entity hit, ProjectileHitEvent event);
    int getHitScore(Entity fromEntity, Entity hitEntity, Team from, Team hit);
    default void tick(){}
    void registerDelayedEvent(Entity from, Entity related, int delay, BukkitRunnable runnable, Predicate<DelayedTriggerEvent> flag);
    void registerTickEvent(Entity from, Entity related, BukkitRunnable runnable, Predicate<TickEvent> shouldRemove);
}
