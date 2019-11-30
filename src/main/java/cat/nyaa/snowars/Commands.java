package cat.nyaa.snowars;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

public class Commands extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public Commands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        PluginCommand snowar = SnowarsPlugin.plugin.getCommand("snowar");
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

    public void reloadCompleter(CommandSender sender, Arguments arguments){

    }

    @SubCommand(value = "", permission = "sw.command", tabCompleter = "Completer")
    public void on(CommandSender sender, Arguments arguments){

    }

    public void Completer(CommandSender sender, Arguments arguments){

    }
}
