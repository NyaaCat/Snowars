package cat.nyaa.snowars;

import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.item.ItemManager;
import cat.nyaa.snowars.item.SnowballHandler;
import cat.nyaa.snowars.item.SnowballManager;
import cat.nyaa.snowars.producer.Producer;
import cat.nyaa.snowars.producer.ProducerManager;
import cat.nyaa.snowars.roller.ItemPoolManager;
import cat.nyaa.snowars.roller.PresentChest;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.UUID;

public class Events implements Listener {

    public Events() {
        SnowarsPlugin plugin = SnowarsPlugin.plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        if (ProducerManager.getInstance().isProducer(rightClicked)) {
            event.setCancelled(true);
            if (!SnowarsPlugin.started) return;
            Producer producer = ProducerManager.getInstance().getProducer(rightClicked);
            producer.onClick(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onUseItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Material im = event.getMaterial();
        if (action == Action.PHYSICAL || im == Material.AIR) return;
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK))
            return;
        ItemManager itemManager = ItemManager.getInstance();
        ItemStack is = event.getItem();
        Optional<AbstractSnowball> item = itemManager.getItem(is);
        if (item.isPresent()) {
            event.setCancelled(true);
            if (!SnowarsPlugin.started) return;
            AbstractSnowball snowballItem = item.get();
            if (snowballItem.onUse(player, event)) {
                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    consumeItem(is);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSnowballHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        SnowballManager snowballManager = SnowballManager.getInstance();
        Optional<SnowballHandler> opt = snowballManager.get(entity);
        if (!opt.isPresent()) {
            return;
        }
        snowballManager.remove(entity);
        SnowballHandler snowballHandler = opt.get();
        String s = entity.getPersistentDataContainer().get(AbstractSnowball.SNOWBALL_FROM, PersistentDataType.STRING);
        if (s == null) return;
        Entity from = entity.getServer().getEntity(UUID.fromString(s));
        if (from == null) return;
        Block hitBlock = event.getHitBlock();
        Entity hitEntity = event.getHitEntity();
        if (!SnowarsPlugin.started) return;
        if (hitBlock != null) {
            snowballHandler.onHitBlock(from, entity, hitBlock, event);
        }
        if (hitEntity != null) {
            if (hitEntity.getScoreboardTags().contains("lucky_entity")) {
                World world = from.getWorld();
                if (hitEntity instanceof Chicken) {
                    world.playSound(from.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 2);
                    world.spawnParticle(Particle.HEART, hitEntity.getLocation(), 20, 1, 1, 1, 0);
                    ScoreManager.getInstance().addFor(from, 5);
                    hitEntity.remove();
                } else if (hitEntity instanceof Rabbit) {
                    world.playSound(from.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 2);
                    world.spawnParticle(Particle.HEART, hitEntity.getLocation(), 20, 1, 1, 1, 0);
                    ScoreManager.getInstance().addFor(from, 3);
                    hitEntity.remove();
                }
            }
            snowballHandler.onHitEntity(from, entity, hitEntity, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeam(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/team")) {
            Utils.clearTeamCache();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeam(ServerCommandEvent event) {
        if (event.getCommand().startsWith("team")) {
            Utils.clearTeamCache();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof Chest) {
            Block block = ((Chest) holder).getBlock();
            if (ItemPoolManager.getInstance().isPoolChest(block)) {
                PresentChest chest = ItemPoolManager.getInstance().getChest(block);
                chest.proceedInv();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDie(EntityDeathEvent event) {
        if (event.getEntity().getScoreboardTags().contains("lucky_entity")) {
            event.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClicked(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof Chest) {
            Block block = ((Chest) holder).getBlock();
            if (ItemPoolManager.getInstance().isPoolChest(block)) {
                PresentChest chest = ItemPoolManager.getInstance().getChest(block);
                if (!chest.onOpen()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void consumeItem(ItemStack is) {
        int amount = is.getAmount();
        is.setAmount(amount - 1);
    }
}
