package cat.nyaa.snowars;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.snowars.item.ItemManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Commands extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public Commands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        PluginCommand snowar = SnowarsPlugin.plugin.getCommand("snowars");
        snowar.setExecutor(this);
        snowar.setTabCompleter(this);
    }

    @Override
    public String getHelpPrefix() {
        return null;
    }

    @SubCommand(value = "reload", permission = "sw.command", tabCompleter = "reloadCompleter")
    public void onReload(CommandSender sender, Arguments arguments){
        SnowarsPlugin.plugin.onReload();
    }

    public List<String> reloadCompleter(CommandSender sender, Arguments arguments){
        List<String> s = new ArrayList<>();
        return s;
    }

    private boolean isPlayer(CommandSender sender) {
        if (sender instanceof Player){
            return true;
        }else {
            new Message("").append(I18n.format("error.not_player")).send(sender);
            return false;
        }
    }

    @SubCommand(value = "getitem", permission = "sw.command", tabCompleter = "getitemCompleter")
    public void onGetItem(CommandSender sender, Arguments arguments){
        if (isPlayer(sender)) {
            Player player = (Player) sender;
            String string = arguments.nextString();
            ItemStack item = ItemManager.getInstance().getItem(string);
            if (!InventoryUtils.addItem(player, item)) {
                player.getWorld().dropItem(player.getEyeLocation(), item);
            }
            new Message("").append(I18n.format("getitem.success"), item).send(sender);
        }
    }

    public List<String> getitemCompleter(CommandSender sender, Arguments arguments){
        List<String> s = new ArrayList<>();
        s.addAll(ItemManager.getInstance().getItemNames());
        return s;
    }

    @SubCommand(value = "", permission = "sw.command", tabCompleter = "Completer")
    public void on(CommandSender sender, Arguments arguments){

    }

    public List<String> Completer(CommandSender sender, Arguments arguments){
        List<String> s = new ArrayList<>();
        return s;
    }
}
