package cat.nyaa.snowars.item.impl;

import cat.nyaa.musicapi.api.IMusicSheet;
import cat.nyaa.musicapi.api.IMusicTask;
import cat.nyaa.musicapi.api.MusicLoader;
import cat.nyaa.musicapi.player.MusicPlayer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.snowars.ScoreManager;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
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

import java.io.File;
import java.io.IOException;

public class SnowballGolden extends AbstractSnowball {
    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        if (ScoreManager.getInstance().isGoldExperienced(from)) {
            return false;
        }
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
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1);
                        return;
                    }
                    if (ticked == 20) {
                        new Message(ChatColor.translateAlternateColorCodes('&', "&e&l黄  金        ")).send(((Player) from), Message.MessageType.SUBTITLE);
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1);
                        return;
                    }
                    if (ticked == 40) {
                        new Message(ChatColor.translateAlternateColorCodes('&', "&e&l黄  金  体    ")).send(((Player) from), Message.MessageType.SUBTITLE);
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1);
                        return;
                    }
                    if (ticked == 60) {
                        world.playSound(from.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 5, 2);
                        new Message(ChatColor.translateAlternateColorCodes('&', "&e&l黄  金  体  验")).send(((Player) from), Message.MessageType.SUBTITLE);
                        int type = ((int) (Math.floor(Utils.random() * 2)));
                        IMusicSheet goldenWind = null;
                        try {
                            switch (type) {
                                case 0:
                                    File file1 = new File(SnowarsPlugin.plugin.getDataFolder(), "golden_wind_1.trak");
                                    goldenWind = MusicLoader.loadFromFile(file1);
                                    break;
                                case 1:
                                    File file2 = new File(SnowarsPlugin.plugin.getDataFolder(), "golden_wind_2.trak");
                                    goldenWind = MusicLoader.loadFromFile(file2);
                                    break;
                            }
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (goldenWind != null){
                            IMusicTask play = new MusicPlayer().play(goldenWind, from);
                            play.play(SnowarsPlugin.plugin);
                        }
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
