package cat.nyaa.snowars;

import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.item.ItemManager;
import cat.nyaa.snowars.item.SnowballHandler;
import cat.nyaa.snowars.item.SnowballManager;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
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
            AbstractSnowball snowballItem = item.get();
            if (snowballItem.onUse(player, event)) {
                if(!player.getGameMode().equals(GameMode.CREATIVE)){
                    consumeItem(is);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSnowballHit(ProjectileHitEvent event){
        Projectile entity = event.getEntity();
        SnowballManager snowballManager = SnowballManager.getInstance();
        Optional<SnowballHandler> opt = snowballManager.get(entity);
        if (!opt.isPresent()) {
            return;
        }
        snowballManager.remove(entity);
        SnowballHandler snowballHandler = opt.get();
        String s = entity.getPersistentDataContainer().get(AbstractSnowball.SNOWBALL_FROM, PersistentDataType.STRING);
        if (s == null)return;
        Entity from = entity.getServer().getEntity(UUID.fromString(s));
        if (from == null)return;
        Block hitBlock = event.getHitBlock();
        Entity hitEntity = event.getHitEntity();
        if (hitBlock != null){
            snowballHandler.onHitBlock(from, entity, hitBlock, event);
        }
        if (hitEntity!=null){
            snowballHandler.onHitEntity(from, entity, hitEntity, event);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeam(PlayerCommandPreprocessEvent event){
        if (event.getMessage().startsWith("/team")) {
            Utils.clearTeamCache();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeam(ServerCommandEvent event){
        if (event.getCommand().startsWith("team")) {
            Utils.clearTeamCache();
        }
    }

    private void consumeItem(ItemStack is) {
        int amount = is.getAmount();
        is.setAmount(amount - 1);
    }
}
