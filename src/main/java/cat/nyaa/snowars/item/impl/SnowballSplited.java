package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Sound;
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
        from.getWorld().playSound(soundLocation, Sound.ENTITY_SNOWBALL_THROW, 0.5f, 0.5f);
        registerDelayedEvent(from, projectile, 8, new BukkitRunnable() {
            @Override
            public void run() {
                Location location = projectile.getLocation();
                Vector velocity = projectile.getVelocity();
                for (int i = 0; i < 10; i++) {
                    Projectile splitedSnowball = launchSnowball(SnowballSplited.this, from, location, velocity, 10, 2d, false);
                    from.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.3f, 0.1f);
                    if (from instanceof Player) {
                        ((Player) from).playSound(soundLocation, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.1f, 0.1f);
                    }
                    Utils.removeLater(splitedSnowball, 8);
                }
                projectile.remove();
            }
        }, delayedTriggerEvent -> !projectile.isDead());
        return true;
    }

    @Override
    public void onLaunch(Entity from, Snowball event) {

    }

    @Override
    public void onHitBlock(Entity from, Entity hit, ProjectileHitEvent event) {

    }

    @Override
    public int getHitScore(Entity fromEntity, Entity hitEntity, Team from, Team hit) {
        return 0;
    }
}
