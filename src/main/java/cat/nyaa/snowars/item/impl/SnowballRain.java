package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class SnowballRain extends AbstractSnowball {
    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        Location eyeLocation = from.getEyeLocation();
        Vector towards = eyeLocation.getDirection().clone();
        double x = towards.getX();
        double z = towards.getZ();
        towards.setY(Math.sqrt(x * x + z * z));
        from.getWorld().playSound(from.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 5, 0.5f);
        Projectile projectile = launchSnowball(this, from, null, towards, 1, 1.5, true);
        registerDelayedEvent(from, projectile, 20, new BukkitRunnable() {
            @Override
            public void run() {
                summonRain(from, projectile);
            }
        }, delayedTriggerEvent -> {
            return !delayedTriggerEvent.getRelated().isDead();
        });
        return true;
    }

    private void summonRain(LivingEntity from, Projectile projectile) {
        Location location = projectile.getLocation();
        Utils.removeNow(projectile);
        Vector downDirection = Utils.y_axis.clone().multiply(-1);
        from.getWorld().spawnParticle(Particle.CLOUD, location, 100, 0.5, 0.5, 0.5, 0.1);
        from.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 5, 1.5f);
        registerTickEvent(from, projectile, new TickTask( tickEvent -> tickEvent.getTicksFromStarted() > 100) {
            @Override
            public void run(int ticks) {
                double r = Utils.random() * 5;
                double theta = Utils.random() * 360;
                Vector vector = new Vector(r, 0, 0);
                vector.rotateAroundAxis(Utils.y_axis, Math.toRadians(theta));
                Location add = location.clone().add(vector);
                if (ticks % 5 == 0) {
                    from.getWorld().playSound(location, Sound.ENTITY_SNOWBALL_THROW, 5 ,1.5f);
                }
                Projectile projectile1 = launchSnowball(SnowballRain.this, from, add, downDirection, 1, 0.8, true);
                projectile1.addScoreboardTag("rain");
            }
        });

    }

    @Override
    public void onLaunch(Entity from, Projectile projectile) {

    }

    @Override
    public void onHitBlock(Entity from, Entity related, Block hit, ProjectileHitEvent event) {
        if (from instanceof LivingEntity && !related.getScoreboardTags().contains("rain")) {
            summonRain(((LivingEntity) from), event.getEntity());
        }
    }

    @Override
    public void onHitEntity(Entity from, Entity related, Entity hit, ProjectileHitEvent event) {
        if (from instanceof LivingEntity && !related.getScoreboardTags().contains("rain")) {
            summonRain(((LivingEntity) from), event.getEntity());
        }else {
            if (hit instanceof LivingEntity) {
                ((LivingEntity) hit).setNoDamageTicks(0);
            }
        }
        super.onHitEntity(from, related, hit, event);
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
