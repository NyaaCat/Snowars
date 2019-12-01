package cat.nyaa.snowars;

import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.item.ItemManager;
import com.sk89q.worldedit.event.platform.CommandEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeam(PlayerCommandPreprocessEvent event){
        if (event.getMessage().startsWith("/team")) {
            AbstractSnowball.clearCache();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeam(ServerCommandEvent event){
        if (event.getCommand().startsWith("team")) {
            AbstractSnowball.clearCache();
        }
    }

    private void consumeItem(ItemStack is) {
        int amount = is.getAmount();
        is.setAmount(amount - 1);
    }
}
