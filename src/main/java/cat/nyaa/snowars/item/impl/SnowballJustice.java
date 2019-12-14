package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.item.AbstractSnowball;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Team;

public class SnowballJustice extends AbstractSnowball {
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

    }

    @Override
    public void onHitEntity(Entity from, Entity related, Entity hit, ProjectileHitEvent event) {
        super.onHitEntity(from, related, hit, event);
        ScoreManager s = ScoreManager.getInstance();
        double fromScore = s.getScore(from);
        double score = s.getScore(hit);
        double total = fromScore + score;
        s.setScore(from, total/2);
        s.setScore(hit, total/2);
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
