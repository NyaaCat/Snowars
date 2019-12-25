package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class SnowballSplited extends AbstractSnowball {
    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        Projectile projectile = launchSnowball(this, from, null, from.getEyeLocation().getDirection(), 1, 1.5d, true);
        Location soundLocation = from.getEyeLocation();
        from.getWorld().playSound(soundLocation, Sound.ENTITY_SNOWBALL_THROW, 5, 0.5f);
        registerDelayedEvent(from, projectile, 8, new BukkitRunnable() {
            @Override
            public void run() {
                Location location = projectile.getLocation();
                Vector velocity = projectile.getVelocity();
                from.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5, 0.1f);
                for (int i = 0; i < 10; i++) {
                    Projectile splitedSnowball = launchSnowball(SnowballSplited.this, from, location, velocity, 15, 2d, false);
                    Utils.removeLater(splitedSnowball, 8);
                }
                Utils.removeNow(projectile);
            }
        }, delayedTriggerEvent -> !projectile.isDead());
        return true;
    }

    @Override
    public void onLaunch(Entity from, Projectile projectile) {

    }

    @Override
    public void onHitBlock(Entity from, Entity related, Block hit, ProjectileHitEvent event) {

    }

    @Override
    public void onHitEntity(Entity from, Entity related, Entity hit, ProjectileHitEvent event) {
        if (hit instanceof LivingEntity) {
            ((LivingEntity) hit).setNoDamageTicks(0);
        }
        super.onHitEntity(from, related, hit, event);
    }

    @Override
    public double getHitScore(Entity fromEntity, Entity hitEntity, Team from, Team hit) {
        return zeroOr(from, hit, 1);
    }

    @Override
    public double getDamage(Entity fromEntity, Entity hitEntity, Team from, Team hit) {
        return zeroOr(from, hit, 5);
    }
}
