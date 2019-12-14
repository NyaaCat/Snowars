package cat.nyaa.snowars.item.impl;

import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Set;

public class SnowballPigeon extends AbstractSnowball {
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
        if(!(hit instanceof Player)){
            return;
        }
        Server server = from.getServer();
        World world = hit.getWorld();
        Scoreboard mainScoreboard = server.getScoreboardManager().getMainScoreboard();
        Team fromTeam = Utils.getTeam(mainScoreboard, from);
        Team hitTeam = Utils.getTeam(mainScoreboard, hit);
        Set<String> entries = hitTeam.getEntries();
        Location originLocation = hit.getLocation();
        AtomicDouble max = new AtomicDouble(0);
        AtomicDouble min = new AtomicDouble(Double.MAX_VALUE);
        ScoreManager instance = ScoreManager.getInstance();
        entries.stream().forEach(s -> {
            Player player = server.getPlayer(s);
            if (player == null)return;
            double score = instance.getScore(player);
            if (score > max.get()){max.set(score);}
            if (score < min.get()){min.set(score);}
        });
        double score = instance.getScore(hit);
        if (score>=max.get()){
           Utils.teleportHome(hit, world);
        }
        if (score<=min.get()){
            PotionEffect effect = PotionEffectType.SPEED.createEffect(30, 3);
            world.playSound(hit.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 10, 1);
            ((LivingEntity) hit).addPotionEffect(effect, true);
        }

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
