package cat.nyaa.snowars;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.snowars.item.ItemManager;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @SubCommand(value = "get", permission = "sw.command", tabCompleter = "getitemCompleter")
    public void onGetItem(CommandSender sender, Arguments arguments){
        if (isPlayer(sender)) {
            Player player = (Player) sender;
            String string = arguments.nextString();
            ItemStack item = ItemManager.getInstance().getItem(string);
            if (!InventoryUtils.addItem(player, item)) {
                player.getWorld().dropItem(player.getEyeLocation(), item);
            }
            new Message("").append(I18n.format("get.success"), item).send(sender);
        }
    }

    public List<String> getitemCompleter(CommandSender sender, Arguments arguments){
        List<String> s = new ArrayList<>();
        s.addAll(ItemManager.getInstance().getItemNames());
        return filtered(arguments, s);
    }

    private static List<String> filtered(Arguments arguments, List<String> completeStr) {
        String next = arguments.at(arguments.length() - 1);
        return completeStr.stream().filter(s -> s.startsWith(next)).collect(Collectors.toList());
    }

    @SubCommand(value = "score", permission = "sw.command", tabCompleter = "scoreCompleter")
    public void onScore(CommandSender sender, Arguments arguments){
        String action = arguments.nextString();
        switch (action){
            case "check":
                onCheck(sender, arguments);
                break;
            case "clear":
                onClear(sender, arguments);
                break;
        }
    }

    private void onClear(CommandSender sender, Arguments arguments) {
        String target = arguments.nextString();
        ScoreManager scoreManager = ScoreManager.getInstance();
        Server server = sender.getServer();
        switch (target){
            case "player":
                Player player = arguments.nextPlayer();
                if (player!= null){
                    scoreManager.clearScore(player);
                    new Message("").append(I18n.format("clear.success", player.getName())).send(sender);
                    return;
                }else {
                    new Message("").append(I18n.format("clear.failed_no_player", player.getName())).send(sender);
                }
                break;
            case "team":
                String teamName = arguments.nextString();
                Scoreboard mainScoreboard = server.getScoreboardManager().getMainScoreboard();
                Team team = mainScoreboard.getTeam(teamName);
                if (team != null){
                    scoreManager.clearScore(team);
                    new Message("").append(I18n.format("clear.team.success", team.getName())).send(sender);
                }else {
                    new Message("").append(I18n.format("clear.team.not_exists", teamName)).send(sender);
                }
                break;
            case "all":
                scoreManager.clearAll();
                new Message("").append(I18n.format("clear.all.success")).send(sender);
                break;
        }
    }

    private void onCheck(CommandSender sender, Arguments arguments) {
        String target = arguments.nextString();
        Server server = sender.getServer();
        Scoreboard mainScoreboard = server.getScoreboardManager().getMainScoreboard();
        ScoreManager scoreManager = ScoreManager.getInstance();
        switch (target){
            case "player":
                Player player = arguments.nextPlayer();
                double score = scoreManager.getScore(player);
                new Message("").append(I18n.format("score.check.player.success", player.getName(), score)).send(sender);
                break;
            case "team":
                String teamName = arguments.nextString();
                Team team = mainScoreboard.getTeam(teamName);
                if (team!=null){
                    new Message("").append(I18n.format("score.check.team.success", teamName)).send(sender);
                    team.getEntries().forEach(s -> {
                        double score1 = scoreManager.getScore(s);
                        new Message("").append(I18n.format("score.check.team", s, score1)).send(sender);
                    });
                }else {
                    new Message("").append(I18n.format("score.check.team_not_exists")).send(sender);
                }
                break;
            case "all":
                break;
        }
    }

    public List<String> scoreCompleter(CommandSender sender, Arguments arguments){
        List<String> s = new ArrayList<>();
        Server server = sender.getServer();
        ScoreManager scoreManager = ScoreManager.getInstance();

        switch (arguments.remains()){
            case 1:
                s.add("check");
                s.add("clear");
                break;
            case 2:
                s.add("player");
                s.add("team");
                s.add("player_team");
                s.add("all");
                break;
            case 3:
                String action1 = arguments.nextString();
                switch (action1){
                    case "check":
                    case "clear":
                        String action2 = arguments.nextString();
                        switch (action2){
                            case "player":
                            case "player_team":
                                Set<String> seta = new HashSet();
                                seta.addAll(server.getOnlinePlayers().stream().map(pl -> pl.getName()).collect(Collectors.toList()));
                                seta.addAll(scoreManager.getPlayers());
                                break;
                            case "team":
                                Scoreboard mainScoreboard = server.getScoreboardManager().getMainScoreboard();
                                s.addAll(mainScoreboard.getTeams().stream().map(team -> team.getName()).collect(Collectors.toList()));
                                break;
                        }
                        break;
                }
        }

        return filtered(arguments, s);
    }

    @SubCommand(value = "damage", permission = "sw.command", tabCompleter = "damageCompleter")
    public void onDamage(CommandSender sender, Arguments arguments){
        Player player = arguments.nextPlayer();
        double v = arguments.nextDouble();
        ScoreManager.getInstance().damage(player, v);
        player.damage(0.1);
    }

    public List<String> damageCompleter(CommandSender sender, Arguments arguments){
        List<String> s = new ArrayList<>();
        switch (arguments.length()){
            case 1:
                s.addAll(SnowarsPlugin.plugin.getServer().getOnlinePlayers().stream().map(player ->((Player) player).getName()).collect(Collectors.toList()));
                break;
            case 2:
                s.add("damage");
                break;
        }
        return filtered(arguments, s);
    }

    @SubCommand(value = "", permission = "sw.command", tabCompleter = "Completer")
    public void on(CommandSender sender, Arguments arguments){

    }

    public List<String> Completer(CommandSender sender, Arguments arguments){
        List<String> s = new ArrayList<>();
        return filtered(arguments, s);
    }
}
