package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Team;

public class SnowballHighspeed extends AbstractSnowball {
    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        Location fromEyeLocation = from.getEyeLocation();
        Projectile projectile = launchSnowball(this, from, null, fromEyeLocation.getDirection(), 1, 5, false);
        from.getWorld().playSound(from.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1f);
        Utils.removeLater(projectile, 3);
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
        return zeroOr(from, hit, 10);
    }
}
