package cat.nyaa.snowars.item.impl;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.snowars.I18n;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class SnowballMine extends AbstractSnowball {
    static ItemStack head = new ItemStack(Material.SNOWBALL);
    static final Vector y_axis = new Vector(0, 1, 0);
    static final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1f);

    @Override
    public boolean onUse(LivingEntity from, PlayerInteractEvent event) {
        Block targetBlock = from.getTargetBlock(null, 5);
        if (targetBlock.getType().equals(Material.AIR)) {
            new Message("").append(I18n.format("snowball.mine.failed_no_target")).send(from);
            return false;
        }
        World world = from.getWorld();
        world.playSound(targetBlock.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.5f);
        ArmorStand armor = world.spawn(targetBlock.getLocation().add(0.5, 0, 0.5), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setSmall(true);
            armorStand.setSilent(true);
            armorStand.setGravity(true);
            armorStand.setChestplate(head);
            armorStand.addScoreboardTag("temp_snow");
        });

        Scoreboard mainScoreboard = from.getServer().getScoreboardManager().getMainScoreboard();
        Team ownerTeam = Utils.getTeam(mainScoreboard, from);
        AtomicBoolean triggered = new AtomicBoolean(false);
        registerDelayedEvent(from, armor, 20, new BukkitRunnable() {
            @Override
            public void run() {
                registerTickEvent(from, armor, new TickTask(tickevent -> triggered.get() || armor.isDead()) {
                    @Override
                    public void run(int ticked) {
                        List<Entity> nearbyEntities = armor.getNearbyEntities(2, 2, 2);
                        if (from instanceof Player) {
                            ((Player) from).spawnParticle(Particle.REDSTONE, armor.getEyeLocation(), 1, 0.5, 0.5, 0.5, 1, dustOptions);
                        }
                        if (nearbyEntities.stream().anyMatch(entity -> {
                            Team t = null;
                            return armor.getEyeLocation().distance(entity.getLocation()) < 1
                                    && entity instanceof LivingEntity
                                    && !(entity instanceof ArmorStand)
                                    && !Objects.equals(Utils.getTeam(mainScoreboard, entity), ownerTeam);
                        })) {
                            trigger(from, armor);
                        }
                    }

                    private void trigger(LivingEntity from, ArmorStand armor) {
                        armor.teleport(armor.getLocation().add(0, 1, 0));
                        armor.setGravity(false);
                        armor.setMarker(true);
                        Location eyeLocation = armor.getEyeLocation();
                        int count = ThreadLocalRandom.current().nextInt(10) + 20;
                        triggered.set(true);
                        for (int i = 0; i < count; i++) {
                            world.spawnParticle(Particle.CLOUD, eyeLocation, 20, 0.5, 0.5, 0.5, 0.3);
                            registerDelayedEvent(from, armor, 2 * i, new BukkitRunnable() {
                                @Override
                                public void run() {
                                    world.playSound(targetBlock.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1.5f);
                                }
                            }, delayedTriggerEvent -> true);
                            launchSnowball(SnowballMine.this, from, eyeLocation, y_axis, 30, 0.5, true);
                        }
                        new Message("").append(I18n.format("snowball.mine.triggered")).send(from);
                        Utils.removeLater(armor, 2 * count);
                    }
                });
            }
        }, event1 -> !armor.isDead());

        new Message("").append(I18n.format("snowball.mine.success")).send(from);
        return true;
    }

    @Override
    public void onLaunch(Entity from, Projectile projectile) {
        projectile.setTicksLived(5);
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
    public void tick() {

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
