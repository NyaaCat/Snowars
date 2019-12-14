package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Objects;

public class SnowballGravity extends AbstractSnowball {
    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        Vector direction = from.getEyeLocation().getDirection();
        launchSnowball(this, from, null, direction, 1, 1.5, true);
        return true;
    }

    @Override
    public void onLaunch(Entity from, Projectile projectile) {

    }

    @Override
    public void onHitBlock(Entity from, Entity related, Block hit, ProjectileHitEvent event) {
        summonGravityField(from, related, hit.getLocation().add(0.5, 0.5, 0.5), 5);
    }

    private void summonGravityField(Entity from, Entity related, Location hit, int radius) {
        World world = from.getWorld();
        Scoreboard mainScoreboard = from.getServer().getScoreboardManager().getMainScoreboard();
        Team fromTeam = Utils.getTeam(mainScoreboard, from);
        registerTickEvent(from, related, new TickTask(tickEvent -> tickEvent.getTicksFromStarted() > 60) {
            @Override
            public void run(int ticked) {
                world.spawnParticle(Particle.PORTAL, hit, 50, 0, 0, 0, 1);
                world.getNearbyEntities(hit, radius, radius, radius).stream()
                        .filter(entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStand))
                        .filter(entity -> entity.getLocation().distance(hit) < radius)
                        .filter(entity -> !Objects.equals(fromTeam, Utils.getTeam(mainScoreboard, entity)))
                        .forEach(entity -> attract(entity, hit.clone(), radius));
            }
        });
        world.playSound(hit, Sound.BLOCK_PORTAL_TRIGGER, 5, 0.8f);
    }

    private void attract(Entity entity, Location location, double radius) {
        double maxSpeed = 0.4d;
        double factor = Math.sqrt(radius - 1.0) / maxSpeed;
        double d = location.distance(entity.getLocation());
        double newVelocity = Math.sqrt(d-1) / factor;
        if (Double.isInfinite(newVelocity) || Double.isNaN(newVelocity)) {
            newVelocity = 0;
        }
        Vector direction = location.subtract(entity.getLocation()).toVector().normalize();
        if (Double.isFinite(direction.getX()) && Double.isFinite(direction.getY()) && Double.isFinite(direction.getZ())){
            entity.setVelocity(direction.multiply(newVelocity));
        }
    }

    @Override
    public void onHitEntity(Entity from, Entity related, Entity hit, ProjectileHitEvent event) {
        super.onHitEntity(from, related, hit, event);
        summonGravityField(from, related, hit.getLocation(), 5);
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
