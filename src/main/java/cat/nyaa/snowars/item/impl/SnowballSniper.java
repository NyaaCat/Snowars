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

public class SnowballSniper extends AbstractSnowball {

    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        launchSnowball(this, from, null, from.getEyeLocation().getDirection(), 1, 2.5d, false);
        Location soundLocation = from.getEyeLocation();
        from.getWorld().playSound(soundLocation, Sound.ENTITY_SNOWBALL_THROW, 0.5f, 1f);
        from.getWorld().playSound(soundLocation, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
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
