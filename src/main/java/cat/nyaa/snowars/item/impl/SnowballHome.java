package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SnowballHome extends AbstractSnowball {
    static List<UUID> homing = new ArrayList<>();

    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        if (homing.contains(from.getUniqueId())){
            return false;
        }
        homing.add(from.getUniqueId());
        Location location = from.getLocation();
        World world = from.getWorld();

        registerTickEvent(from, null, new TickTask(tickEvent -> tickEvent.getTicksFromStarted()>100) {
            @Override
            public void run(int ticked) {
                Location location1 = from.getLocation();
                World world1 = from.getWorld();
                world1.spawnParticle(Particle.END_ROD, location1, 20, 0, 0, 0, 0.2, null);
            }
        });
        new BukkitRunnable(){
            @Override
            public void run() {
                Projectile related = launchSnowball(SnowballHome.this, from, from.getLocation(), Utils.y_axis.clone(), 0, 1, true);
                world.playSound(from.getEyeLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.5f, 1f);
                Utils.teleportHome(from, world);
                registerDelayedEvent(from, related, 20, new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location location1 = related.getLocation();
                        world.playSound(location1, Sound.ENTITY_GENERIC_EXPLODE, 5, 1);
                        world.spawnParticle(Particle.CLOUD, location1, 100, 0.5, 0.5, 0.5, 0.2, null);
                        for (int i = 0; i < 40; i++) {
                            launchSnowball(SnowballHome.this, from, location1, Utils.y_axis.clone(), 180, 0.5, true);
                        }
                        Utils.removeNow(related);
                        homing.remove(from.getUniqueId());
                    }
                }, delayedTriggerEvent -> true);
            }
        }.runTaskLater(SnowarsPlugin.plugin, 100);
        return true;
    }

    @Override
    public void onLaunch(Entity from, Projectile projectile) {

    }

    @Override
    public void onHitBlock(Entity from, Entity related, Block hit, ProjectileHitEvent event) {

    }

    @Override
    public double getHitScore(Entity fromEntity, Entity hitEntity, Team from, Team hit) {
        return zeroOr(from, hit, 1);
    }

    @Override
    public double getDamage(Entity fromEntity, Entity hitEntity, Team from, Team hit) {
        return zeroOr(from, hit, 10);
    }
}
