package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;

public class SnowballCucumber extends AbstractSnowball {
    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        launchSnowball(this, from, null, from.getEyeLocation().getDirection(), 1, 1.5, true);
        return true;
    }

    @Override
    public void onLaunch(Entity from, Projectile projectile) {

    }

    @Override
    public void onHitBlock(Entity from, Entity related, Block hit, ProjectileHitEvent event) {
        trigger(from, related, hit.getLocation().clone().add(0.5, 0.5, 0.5), 3);
    }

    private void trigger(Entity from, Entity related, Location location, int i) {
        World world = from.getWorld();
        AtomicInteger score = new AtomicInteger(0);
        ScoreManager sm = ScoreManager.getInstance();
        world.getNearbyEntities(location, i, i, i).stream()
                .filter(entity -> entity  instanceof LivingEntity && !(entity instanceof ArmorStand))
                .filter(entity -> entity.getLocation().distance(location) < 3)
                .forEach(entity -> {
                    Vector velocity = entity.getVelocity();
                    entity.setVelocity(velocity.setY(1.4));
                    Team team = Utils.getTeam(from);
                    Team hitTeam = Utils.getTeam(entity);
                    score.getAndAdd((int) zeroOr(team, hitTeam, 1));
                    sm.damage(entity, zeroOr(team, hitTeam, getDamage(from, entity, team, hitTeam)));
                });
        int sc = score.get();
        if (sc > 6) sc = 32;
        else sc = 1 << Math.max(0, sc - 1);
        sm.addFor(from, sc);
    }

    @Override
    public void onHitEntity(Entity from, Entity related, Entity hit, ProjectileHitEvent event) {
        super.onHitEntity(from, related, hit, event);
        trigger(from, related, hit.getLocation(), 5);
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
