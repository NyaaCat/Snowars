package cat.nyaa.snowars.item.impl;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.item.AbstractSnowball;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Team;

public class SnowballGolden extends AbstractSnowball {
    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        Projectile related = launchSnowball(this, from, null, from.getEyeLocation().getDirection(), 1, 1.5, true);
        final int duartion = 600;
        if (from instanceof Player) {
            ScoreManager.getInstance().goldExperienced(from, duartion);
        }
        if (from instanceof Player) {
            World world = from.getWorld();
            registerTickEvent(from, related, new TickTask(tickEvent -> tickEvent.getTicksFromStarted() > duartion) {
                @Override
                public void run(int ticked) {
                    if (ticked == 1) {
                        new Message(ChatColor.translateAlternateColorCodes('&', "&e&l黄            ")).send(((Player) from), Message.MessageType.SUBTITLE);
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 10, 1);
                        return;
                    }
                    if (ticked == 20) {
                        new Message(ChatColor.translateAlternateColorCodes('&', "&e&l黄  金        ")).send(((Player) from), Message.MessageType.SUBTITLE);
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 10, 1);
                        return;
                    }
                    if (ticked == 40) {
                        new Message(ChatColor.translateAlternateColorCodes('&', "&e&l黄  金  体    ")).send(((Player) from), Message.MessageType.SUBTITLE);
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 10, 1);
                        return;
                    }
                    if (ticked == 60) {
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 10, 2);
                        new Message(ChatColor.translateAlternateColorCodes('&', "&e&l黄  金  体  验")).send(((Player) from), Message.MessageType.SUBTITLE);
                    }
                }
            });
        }
        return true;
    }

    @Override
    public void onLaunch(Entity from, Projectile projectile) {

    }

    @Override
    public void onHitBlock(Entity from, Entity related, Block hit, ProjectileHitEvent event) {

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
