package cat.nyaa.snowars.item;

import cat.nyaa.snowars.event.DelayedTriggerEvent;
import cat.nyaa.snowars.event.TickTask;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.function.Predicate;

public interface SnowballHandler {
    boolean onUse(LivingEntity from, PlayerInteractEvent event);
    void onLaunch(Entity from, Projectile projectile);
    void onHitEntity(Entity from, Entity related, Entity hit, ProjectileHitEvent event);
    void onHitBlock(Entity from, Entity related, Block hit, ProjectileHitEvent event);
    double getHitScore(Entity fromEntity, Entity hitEntity, Team from, Team hit);
    double getDamage(Entity fromEntity, Entity hitEntity, Team from, Team hit);
    default void tick(){}
    void registerDelayedEvent(Entity from, Entity related, int delay, BukkitRunnable runnable, Predicate<DelayedTriggerEvent> flag);
    void registerTickEvent(Entity from, Entity related, TickTask tickTask);
}
