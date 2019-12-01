package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.item.AbstractSnowball;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Team;

public class SnowballNormal extends AbstractSnowball {

    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        launchSnowball(this, from, null, from.getEyeLocation().getDirection(), 1, 1.5d, true);
        Location soundLocation = from.getEyeLocation();
        from.getWorld().playSound(soundLocation, Sound.ENTITY_SNOWBALL_THROW, 0.5f, 0.5f);
        return true;
    }

    @Override
    public void onLaunch(Entity from, Snowball event) {

    }

    @Override
    public void onHitEntity(Entity from, Entity hit, ProjectileHitEvent event) {
        super.onHitEntity(from,hit,event);
    }

    @Override
    public void onHitBlock(Entity from, Entity hit, ProjectileHitEvent event) {

    }

    @Override
    public int getHitScore(Entity fromEntity, Entity hitEntity, Team from, Team hit) {
        return zeroOr(from, hit, 1);
    }
}
