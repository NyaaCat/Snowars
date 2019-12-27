package cat.nyaa.snowars;

import cat.nyaa.musicapi.api.IMusicSheet;
import cat.nyaa.musicapi.api.IMusicTask;
import cat.nyaa.musicapi.api.MusicLoader;
import cat.nyaa.musicapi.player.MusicPlayer;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.snowars.config.ProducerConfig;
import cat.nyaa.snowars.config.RegionConfig;
import cat.nyaa.snowars.event.AutoSpawnTask;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.item.ItemManager;
import cat.nyaa.snowars.producer.Producer;
import cat.nyaa.snowars.producer.ProducerManager;
import cat.nyaa.snowars.roller.ItemPool;
import cat.nyaa.snowars.roller.ItemPoolManager;
import cat.nyaa.snowars.roller.PresentChest;
import cat.nyaa.snowars.ui.HealthUi;
import cat.nyaa.snowars.utils.RegionManager;
import cat.nyaa.snowars.utils.Utils;
import com.google.common.util.concurrent.AtomicDouble;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.util.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Commands extends CommandReceiver {
    ILocalizer i18N;

    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public Commands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
        this.i18N = _i18n;
        PluginCommand snowar = SnowarsPlugin.plugin.getCommand("snowars");
        snowar.setExecutor(this);
        snowar.setTabCompleter(this);
        producerCommand = new ProducerCommand(SnowarsPlugin.plugin, this.i18N);
        regionCommand = new RegionCommand(SnowarsPlugin.plugin, this.i18N);
        itemPoolCommand = new ItemPoolCommand(SnowarsPlugin.plugin, this.i18N);
        debugCommand = new DebugCommand(SnowarsPlugin.plugin, this.i18N);
    }

    @Override
    public String getHelpPrefix() {
        return null;
    }

    @SubCommand(value = "reload", permission = "sw.command", tabCompleter = "reloadCompleter")
    public void onReload(CommandSender sender, Arguments arguments) {
        SnowarsPlugin.plugin.onReload();
    }

    public List<String> reloadCompleter(CommandSender sender, Arguments arguments) {
        List<String> s = new ArrayList<>();
        return s;
    }

    private boolean isPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        } else {
            new Message("").append(I18n.format("error.not_player")).send(sender);
            return false;
        }
    }

    @SubCommand(value = "get", permission = "sw.command", tabCompleter = "getitemCompleter")
    public void onGetItem(CommandSender sender, Arguments arguments) {
        if (isPlayer(sender)) {
            Player player = (Player) sender;
            String string = arguments.nextString();
            ItemStack item = ItemManager.getInstance().getItem(string);
            if (!InventoryUtils.addItem(player, item)) {
                player.getWorld().dropItem(player.getEyeLocation(), item);
            }
            new Message("").append(I18n.format("get.success")).append(item).send(sender);
        }
    }

    public List<String> getitemCompleter(CommandSender sender, Arguments arguments) {
        List<String> s = new ArrayList<>();
        s.addAll(ItemManager.getInstance().getItemNames());
        return filtered(arguments, s);
    }

    private static List<String> filtered(Arguments arguments, List<String> completeStr) {
        String next = arguments.at(arguments.length() - 1);
        return completeStr.stream().filter(s -> s.startsWith(next)).collect(Collectors.toList());
    }

    @SubCommand(value = "score", permission = "sw.command", tabCompleter = "scoreCompleter")
    public void onScore(CommandSender sender, Arguments arguments) {
        String action = arguments.nextString();
        switch (action) {
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
        switch (target) {
            case "player":
                Player player = arguments.nextPlayer();
                if (player != null) {
                    scoreManager.clearScore(player);
                    new Message("").append(I18n.format("clear.success", player.getName())).send(sender);
                    return;
                } else {
                    new Message("").append(I18n.format("clear.failed_no_player", player.getName())).send(sender);
                }
                break;
            case "team":
                String teamName = arguments.nextString();
                Scoreboard mainScoreboard = server.getScoreboardManager().getMainScoreboard();
                Team team = mainScoreboard.getTeam(teamName);
                if (team != null) {
                    scoreManager.clearScore(team);
                    new Message("").append(I18n.format("clear.team.success", team.getName())).send(sender);
                } else {
                    new Message("").append(I18n.format("clear.team.not_exists", teamName)).send(sender);
                }
                break;
            case "all":
                scoreManager.clearAll();
                new Message("").append(I18n.format("clear.all.success")).send(sender);
                break;
        }
        ScoreManager.getInstance().save();
    }

    private void onCheck(CommandSender sender, Arguments arguments) {
        String target = arguments.nextString();
        Server server = sender.getServer();
        Scoreboard mainScoreboard = server.getScoreboardManager().getMainScoreboard();
        ScoreManager scoreManager = ScoreManager.getInstance();
        switch (target) {
            case "player":
                Player player = arguments.nextPlayer();
                double score = scoreManager.getScore(player);
                new Message("").append(I18n.format("score.check.player.success", player.getName(), score)).send(sender);
                break;
            case "team":
                String teamName = arguments.nextString();
                Team team = mainScoreboard.getTeam(teamName);
                if (team != null) {
                    AtomicDouble sc = new AtomicDouble(0);
                    team.getEntries().forEach(s -> {
                        double score1 = scoreManager.getScore(s);
                        sc.addAndGet(score1);
                        new Message("").append(I18n.format("score.check.team.info", s, score1)).send(sender);
                    });
                    new Message("").append(I18n.format("score.check.team.success", teamName, sc.get())).send(sender);
                } else {
                    new Message("").append(I18n.format("score.check.team_not_exists", teamName)).send(sender);
                }
                break;
            case "all":
                break;
        }
        ScoreManager.getInstance().save();
    }

    public List<String> scoreCompleter(CommandSender sender, Arguments arguments) {
        List<String> s = new ArrayList<>();
        Server server = sender.getServer();
        ScoreManager scoreManager = ScoreManager.getInstance();

        switch (arguments.remains()) {
            case 1:
                s.add("check");
                s.add("clear");
                break;
            case 2:
                s.add("player");
                s.add("team");
                s.add("all");
                break;
            case 3:
                String action1 = arguments.nextString();
                switch (action1) {
                    case "check":
                    case "clear":
                        String action2 = arguments.nextString();
                        switch (action2) {
                            case "player":
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
    public void onDamage(CommandSender sender, Arguments arguments) {
        Player player = arguments.nextPlayer();
        double v = arguments.nextDouble();
        ScoreManager.getInstance().damage(player, v);
//        player.damage(0.1);
    }

    public List<String> damageCompleter(CommandSender sender, Arguments arguments) {
        List<String> s = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                s.addAll(SnowarsPlugin.plugin.getServer().getOnlinePlayers().stream().map(player -> ((Player) player).getName()).collect(Collectors.toList()));
                break;
            case 2:
                s.add("damage");
                break;
        }
        return filtered(arguments, s);
    }

    @SubCommand(value = "start", permission = "sw.command")
    public void onStart(CommandSender sender, Arguments arguments) {
        producerCommand.onStart(sender, arguments);
        World world = null;
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else if (sender instanceof BlockCommandSender) {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        }
        if (world != null) {
            List<Player> players = world.getPlayers();
            List<Player> collect = players.stream()
                    .filter(player -> Utils.getTeam(player) != null).collect(Collectors.toList());
            teleportPlayers(collect);
            clearBackpack(collect);
            clearTaggedEntities(world);
            Collection<Producer> producers = ProducerManager.getInstance().getProducers();
            Collection<PresentChest> chests = ItemPoolManager.getInstance().getChests();
            chests.forEach(presentChest -> presentChest.clear());
            producers.forEach(Producer::clear);
            Message title = new Message("").append(ChatColor.translateAlternateColorCodes('&', I18n.format("title.game_start")));
            players.forEach(player -> title.send(player, Message.MessageType.TITLE));
        }
    }

    private void clearTaggedEntities(World world) {
        String [] tags = {"temp_snow", "bonus_socks", "lucky_entity"};
        world.getEntities().stream().filter(entity -> {
            boolean b = false;
            Set<String> scoreboardTags = entity.getScoreboardTags();
            for (int i = 0; i < tags.length; i++) {
                b = b || scoreboardTags.contains(tags[i]);
            }
            return b;
        }).forEach(Entity::remove);
        ProducerManager.getInstance().clearSocks();
    }

    private void clearBackpack(List<Player> collect) {
        collect.forEach(player -> player.getInventory().clear());
    }

    @SubCommand(value = "stop", permission = "sw.command", tabCompleter = "stopCompleter")
    public void onStop(CommandSender sender, Arguments arguments) {
        producerCommand.onStop(sender, arguments);
        World world = null;
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else if (sender instanceof BlockCommandSender) {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        }
        if (world != null) {
            RegionConfig region = null;
            if (arguments.top()!= null){
                String s = arguments.nextString();
                region = RegionManager.getInstance().getRegion(s);
                if (region == null){
                    new Message("").append(I18n.format("stop.error_no_region", s)).send(sender);
                    return;
                }
            }
            List<Player> players = world.getPlayers();
            Message title = new Message("").append(ChatColor.translateAlternateColorCodes('&', I18n.format("title.game_stop")));
            players.forEach(player -> title.send(player, Message.MessageType.TITLE));
            clearTaggedEntities(world);
            if (region != null){
               RegionConfig finalRegion = region;
               players.forEach(player -> teleportPlayers(players, finalRegion));
            }
        }
    }

    private void teleportPlayers(List<Player> players, RegionConfig finalRegion) {
        AtomicInteger integer = new AtomicInteger(0);
        players.forEach(player -> {
            if (finalRegion != null) {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for (int i = 0; i < 20; i++) {
                            Location location = Utils.randomLocation(finalRegion.region);
                            if (location != null) {
                                player.teleport(location);
                                break;
                            }
                        }
                    }
                }.runTaskLater(SnowarsPlugin.plugin, integer.getAndAdd(1));
            }
        });
    }

    public List<String> stopCompleter(CommandSender sender, Arguments arguments) {
        List<String> s = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                s.addAll(RegionManager.getInstance().getRegionNames());
                break;
        }
        return filtered(arguments, s);
    }

    private void teleportPlayers(List<Player> world) {
        world.forEach(player -> {
            RegionConfig teamRegion = RegionManager.getInstance().getTeamRegion(Utils.getTeam(player));
            if (teamRegion != null) {
                for (int i = 0; i < 20; i++) {
                    Location location = Utils.randomLocation(teamRegion.region);
                    if (location != null) {
                        player.teleport(location);
                        break;
                    }
                }
            }
        });
    }

    @SubCommand(value = "producer", permission = "sw.command")
    public ProducerCommand producerCommand;

    @SubCommand(value = "region", permission = "sw.command")
    public RegionCommand regionCommand;

    @SubCommand(value = "pool", permission = "sw.command")
    public ItemPoolCommand itemPoolCommand;

    @SubCommand(value = "debug", permission = "sw.command")
    public DebugCommand debugCommand;

    @SubCommand(value = "join", permission = "sw.command")
    public void onJoin(CommandSender sender, Arguments arguments) {
        Player player = arguments.nextPlayer();
        Team team = Utils.getTeam(player);
        if (team == null){
            Team selected = Utils.chooseTeam();
            if (selected != null){
                selected.addEntry(player.getName());
                Utils.teleportHome(player, player.getWorld());
                new Message("").append(I18n.format("join.success", player.getName(), selected.getName())).send(sender);
            }else {
                new Message("").append(I18n.format("join.failed_no_team")).send(sender);
            }
        }else {
            Utils.teleportHome(player, player.getWorld());
        }
    }


    @SubCommand(value = "magnification", permission = "sw.command", tabCompleter = "magnificationCompleter")
    public void onMagnification(CommandSender sender, Arguments arguments) {
        double magnification = arguments.nextDouble();
        Configurations configurations = SnowarsPlugin.plugin.configurations;
        configurations.magnification = magnification;
        configurations.save();
    }

    @SubCommand(value = "damageAmplifier", permission = "sw.command")
    public void onDamageAmplifier(CommandSender sender, Arguments arguments) {
        double magnification = arguments.nextDouble();
        Configurations configurations = SnowarsPlugin.plugin.configurations;
        configurations.damageAmplifier = magnification;
        configurations.save();
    }


    public List<String> magnificationCompleter(CommandSender sender, Arguments arguments) {
        List<String> s = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                s.add("<magnification>");
                break;
        }
        return filtered(arguments, s);
    }

    //    @SubCommand(value = "", permission = "sw.command", tabCompleter = "Completer")
    public void on(CommandSender sender, Arguments arguments) {

    }

    public List<String> Completer(CommandSender sender, Arguments arguments) {
        List<String> s = new ArrayList<>();
        return filtered(arguments, s);
    }

    public static class ProducerCommand extends CommandReceiver {
        /**
         * @param plugin for logging purpose only
         * @param _i18n
         */
        public ProducerCommand(Plugin plugin, ILocalizer _i18n) {
            super(plugin, _i18n);
        }


        @SubCommand(value = "start", permission = "sw.command")
        public void onStart(CommandSender sender, Arguments arguments) {
            ProducerManager.getInstance().start();
            ProducerManager.getInstance().save();
            SnowarsPlugin.started = true;
            AutoSpawnTask.start();
        }

        @SubCommand(value = "stop", permission = "sw.command")
        public void onStop(CommandSender sender, Arguments arguments) {
            ProducerManager.getInstance().stop();
            ProducerManager.getInstance().save();
            SnowarsPlugin.started = false;
            AutoSpawnTask.stop();
        }

        @SubCommand(value = "summon", permission = "sw.command", tabCompleter = "summonCompleter")
        public void onSummon(CommandSender sender, Arguments arguments) {
            if (sender instanceof Player) {
                String producerConfigName = arguments.nextString();
                ProducerConfig producerConfig = ProducerManager.getInstance().getConfig(producerConfigName);
                if (producerConfig == null) {
                    new Message(I18n.format("producer.summon.error_no_config", producerConfigName)).send(sender);
                    return;
                }
                Block targetBlock = ((Player) sender).getTargetBlock(null, 10);
                Location spawnLoc = targetBlock.getLocation().add(0.5, 1, 0.5);
                ProducerManager.getInstance().summonProducer(spawnLoc, producerConfig);
                new Message(I18n.format("summon.success", producerConfigName)).send(sender);
                ProducerManager.getInstance().save();
            }
        }

        public List<String> summonCompleter(CommandSender sender, Arguments arguments) {
            List<String> s = new ArrayList<>();
            switch (arguments.remains()) {
                case 1:
                    s.addAll(ProducerManager.getInstance().getConfigNames());
                    break;
            }
            return filtered(arguments, s);
        }

        @SubCommand(value = "destroy", permission = "sw.command")
        public void onDestroy(CommandSender sender, Arguments arguments) {
            if (sender instanceof Player) {
                Location location = ((Player) sender).getLocation();
                List<Entity> nearbyEntities = ((Player) sender).getNearbyEntities(5, 5, 5);
                Entity toDestroy = nearbyEntities.stream().filter(entity -> ProducerManager.getInstance().isProducer(entity))
                        .min(Comparator.comparingDouble(e -> e.getLocation().distance(location))).orElse(null);
                if (toDestroy == null) {
                    new Message(I18n.format("destroy.failed")).send(sender);
                    return;
                }
                ProducerManager.getInstance().destroy(toDestroy);
                new Message(I18n.format("destroy.success")).send(sender);
            }
        }

        @SubCommand(value = "define", permission = "sw.command", tabCompleter = "defineCompleter")
        public void onDefine(CommandSender sender, Arguments arguments) {
            String name = arguments.nextString();
            double capacity = arguments.nextDouble();
            double current = arguments.nextDouble();
            double productionSpeed = arguments.nextDouble();
            ItemStack itemStack = null;
            if (arguments.remains() > 0) {
                String itemType = arguments.nextString();
                if (itemType.equals("hand")) {
                    if (sender instanceof Player) {
                        itemStack = ((Player) sender).getInventory().getItemInMainHand();
                    }
                } else {
                    itemStack = ItemManager.getInstance().getItem(itemType);
                }
            } else {
                itemStack = ItemManager.getInstance().getItem("normal");
            }
            if (itemStack == null) {
                new Message(I18n.format("producer.define.error_no_item", name)).send(sender);
                return;
            }
            ProducerManager.getInstance().define(name, capacity, current, productionSpeed, itemStack);
            new Message(I18n.format("producer.define.success", name)).append(itemStack).send(sender);
        }

        @SubCommand(value = "remove", permission = "sw.command", tabCompleter = "removeCompleter")
        public void onRemove(CommandSender sender, Arguments arguments) {
            String name = arguments.nextString();
            ProducerConfig config = ProducerManager.getInstance().getConfig(name);
            if (config == null) {
                new Message("").append(I18n.format("producer.remove.not_exists", name)).send(sender);
                return;
            }
            ProducerManager.getInstance().remove(name);
            new Message("").append(I18n.format("producer.remove.success", name)).send(sender);
        }

        public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
            List<String> s = new ArrayList<>();
            switch (arguments.remains()) {
                case 1:
                    s.addAll(ProducerManager.getInstance().getConfigNames());
                    break;
            }
            return filtered(arguments, s);
        }

        public List<String> defineCompleter(CommandSender sender, Arguments arguments) {
            List<String> s = new ArrayList<>();
            switch (arguments.remains()) {
                case 1:
                    s.add("name");
                    break;
                case 2:
                    s.add("capacity");
                    break;
                case 3:
                    s.add("current");
                    break;
                case 4:
                    s.add("productionSpeed");
                    break;
                case 5:
                    s.addAll(ItemManager.getInstance().getItemNames());
                    s.add("hand");
                    break;
            }
            return filtered(arguments, s);
        }

        @Override
        public String getHelpPrefix() {
            return null;
        }
    }

    public static class RegionCommand extends CommandReceiver {

        public RegionCommand(Plugin plugin, ILocalizer _i18n) {
            super(plugin, _i18n);
        }

        @SubCommand(value = "add", permission = "sw.command", tabCompleter = "addCompleter")
        public void onAdd(CommandSender sender, Arguments arguments) {
            String name = arguments.nextString();
            if (RegionManager.getInstance().contains(name)) {
                new Message(I18n.format("region.add.failed_exists", name)).send(sender);
                return;
            }
            try {
                WorldEditPlugin worldEditPlugin = WorldEditPlugin.getPlugin(WorldEditPlugin.class);
                if (!worldEditPlugin.isEnabled()) {
                    new Message(I18n.format("error.we_not_enabled", name)).send(sender);
                    return;
                }
                LocalSession session = worldEditPlugin.getSession((Player) sender);
                World world = ((Player) sender).getWorld();
                Region selection = session.getSelection(new BukkitWorld(world));
                BlockVector3 minimumPoint = selection.getMinimumPoint();
                BlockVector3 maximumPoint = selection.getMaximumPoint();
                RegionConfig.Region region = new RegionConfig.Region(world,
                        minimumPoint.getX(), maximumPoint.getX(),
                        minimumPoint.getY(), maximumPoint.getY(),
                        minimumPoint.getZ(), maximumPoint.getZ());
                RegionManager.getInstance().addRegion(region, name);
                new Message(I18n.format("region.add.success", name)).send(sender);
            } catch (LinkageError e) {
                new Message(I18n.format("error.we_not_enabled", name)).send(sender);
            } catch (IncompleteRegionException e) {
                new Message(I18n.format("create.error.no_selection")).send(sender);
            }
        }

        public List<String> addCompleter(CommandSender sender, Arguments arguments) {
            List<String> s = new ArrayList<>();
            switch (arguments.remains()) {
                case 1:
                    s.add("<name>");
                    break;
            }
            return filtered(arguments, s);
        }

        @SubCommand(value = "remove", permission = "sw.command", tabCompleter = "removeCompleter")
        public void onRemove(CommandSender sender, Arguments arguments) {
            String name = arguments.nextString();
            RegionConfig region = RegionManager.getInstance().getRegion(name);
            if (region == null) {
                new Message(I18n.format("region.remove.failed_not_exist", name)).send(sender);
                return;
            }
            RegionManager.getInstance().removeRegion(name);
            new Message(I18n.format("region.remove.success", name)).send(sender);
        }

        public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
            List<String> s = new ArrayList<>();
            switch (arguments.remains()) {
                case 1:
                    s.addAll(RegionManager.getInstance().getRegionNames());
                    break;
            }
            return filtered(arguments, s);
        }

        @SubCommand(value = "setTeam", permission = "sw.command", tabCompleter = "setTeamCompleter")
        public void onSetTeam(CommandSender sender, Arguments arguments) {
            String teamName = arguments.nextString();
            String regionName = arguments.nextString();

            RegionConfig regionConf = RegionManager.getInstance().getRegion(regionName);
            Scoreboard mainScoreboard = SnowarsPlugin.plugin.getServer().getScoreboardManager().getMainScoreboard();
            Team team = mainScoreboard.getTeam(teamName);
            if (regionConf == null) {
                new Message(I18n.format("region.set_team.error_no_region", regionName)).send(sender);
                return;
            }
            if (team == null) {
                new Message(I18n.format("region.set_team.error_no_team", teamName)).send(sender);
                return;
            }
            RegionManager.getInstance().setTeam(regionName, team);
            new Message(I18n.format("region.set_team.success", teamName, regionName)).send(sender);
        }

        public List<String> setTeamCompleter(CommandSender sender, Arguments arguments) {
            List<String> s = new ArrayList<>();
            switch (arguments.remains()) {
                case 1:
                    List<String> collect = SnowarsPlugin.plugin.getServer().getScoreboardManager().getMainScoreboard().getTeams().stream()
                            .map(team -> team.getName()).collect(Collectors.toList());
                    s.addAll(collect);
                    break;
                case 2:
                    s.addAll(RegionManager.getInstance().getRegionNames());
                    break;
            }
            return filtered(arguments, s);
        }

        @SubCommand(value = "autoSpawn", permission = "sw.command", tabCompleter = "autoSpawnCompleter")
        public void onAutoSpawn(CommandSender sender, Arguments arguments) {
            String action = arguments.nextString();
            String regionName = arguments.nextString();
            String producerName = arguments.nextString();
            RegionManager regionManager = RegionManager.getInstance();
            ProducerManager producerManager = ProducerManager.getInstance();

            RegionConfig region = regionManager.getRegion(regionName);
            ProducerConfig producer = producerManager.getConfig(producerName);

            if (region == null) {
                new Message("").append(I18n.format("region.auto_spawn.error_no_region", regionName)).send(sender);
                return;
            }
            if (producer == null) {
                new Message("").append(I18n.format("region.auto_spawn.error_no_producer", producerName)).send(sender);
                return;
            }

            switch (action) {
                case "add":
                    if (region.containsAutoSpawn(producerName)) {
                        new Message("").append(I18n.format("region.auto_spawn.producer_exists", producerName)).send(sender);
                        return;
                    }
                    region.addAutoSpawn(producerName);
                    RegionManager.getInstance().save();
                    new Message("").append(I18n.format("region.auto_spawn.add.success", regionName, producerName)).send(sender);
                    break;
                case "remove":
                    if (!region.containsAutoSpawn(producerName)) {
                        new Message("").append(I18n.format("region.auto_spawn.error_no_producer", producerName)).send(sender);
                        return;
                    }
                    new Message("").append(I18n.format("region.auto_spawn.remove.success", regionName, producerName)).send(sender);
                    region.removeAutoSpawn(producerName);
                    RegionManager.getInstance().save();
                    break;
            }

        }

        public List<String> autoSpawnCompleter(CommandSender sender, Arguments arguments) {
            List<String> s = new ArrayList<>();
            String action1;
            switch (arguments.remains()) {
                case 1:
                    s.add("add");
                    s.add("remove");
                    break;
                case 2:
                    s.add("<region_name>");
                    s.addAll(RegionManager.getInstance().getRegionNames());
                    break;
                case 3:
                    action1 = arguments.nextString();
                    String regionName = arguments.nextString();
                    RegionConfig region = RegionManager.getInstance().getRegion(regionName);
                    switch (action1) {
                        case "add":
                            s.addAll(ProducerManager.getInstance().getConfigNames());
                            break;
                        case "remove":
                            if (region != null) {
                                s.addAll(region.getAutoSpawns());
                            }
                            break;
                    }
                    break;

            }
            return filtered(arguments, s);
        }

        @Override
        public String getHelpPrefix() {
            return null;
        }
    }

    public static class ItemPoolCommand extends CommandReceiver {
        private Plugin plugin;
        private ILocalizer _i18n;

        public ItemPoolCommand(Plugin plugin, ILocalizer _i18n) {
            super(plugin, _i18n);
            this.plugin = plugin;
            this._i18n = _i18n;
            poolCommand = new PoolCommand(this.plugin, this._i18n);
            itemCommand = new ItemCommand(this.plugin, this._i18n);
            rollerCommand = new RollerCommand(this.plugin, this._i18n);
        }

        @SubCommand(value = "pool", permission = "sw.command")
        PoolCommand poolCommand;
        @SubCommand(value = "item", permission = "sw.command")
        ItemCommand itemCommand;
        @SubCommand(value = "roller", permission = "sw.command")
        RollerCommand rollerCommand;

        public static class PoolCommand extends CommandReceiver {
            /**
             * @param plugin for logging purpose only
             * @param _i18n
             */
            public PoolCommand(Plugin plugin, ILocalizer _i18n) {
                super(plugin, _i18n);
            }

            @SubCommand(value = "create", permission = "sw.command", tabCompleter = "createCompleter")
            public void onCreate(CommandSender sender, Arguments arguments) {
                String name = arguments.nextString();
                ItemPool pool = ItemPoolManager.getInstance().getPool(name);
                if (pool != null) {
                    new Message(I18n.format("pool.create.error_exists", name)).send(sender);
                    return;
                }
                ItemPoolManager.getInstance().createPool(name);
                new Message(I18n.format("pool.create.success", name)).send(sender);
            }

            public List<String> createCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                s.add("name");
                return filtered(arguments, s);
            }

            @SubCommand(value = "remove", permission = "sw.command", tabCompleter = "removeCompleter")
            public void onRemove(CommandSender sender, Arguments arguments) {
                String name = arguments.nextString();
                ItemPool pool = ItemPoolManager.getInstance().getPool(name);
                if (pool == null) {
                    new Message("").append(I18n.format("pool.remove.error_not_exists", name)).send(sender);
                    return;
                }
                ItemPoolManager.getInstance().removePool(name);
                new Message("").append(I18n.format("pool.remove.success", name)).send(sender);
            }

            public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                switch (arguments.remains()) {
                    case 1:
                        s.addAll(ItemPoolManager.getInstance().getPoolNames());
                        break;
                }
                return filtered(arguments, s);
            }

            @SubCommand(value = "addItem", permission = "sw.command", tabCompleter = "addItemCompleter")
            public void onAddItem(CommandSender sender, Arguments arguments) {
                String poolName = arguments.nextString();
                String itemName = arguments.nextString();
                int weight = arguments.nextInt();
                ItemPool pool = ItemPoolManager.getInstance().getPool(poolName);
                if (pool == null) {
                    new Message("").append(I18n.format("pool.add_item.error_no_pool", poolName, itemName)).send(sender);
                    return;
                }
                if (pool.contains(itemName)) {
                    new Message("").append(I18n.format("pool.add_item.error_item_exist", poolName, itemName)).send(sender);
                    return;
                }
                pool.addItem(itemName, weight);
                new Message("").append(I18n.format("pool.add_item.success", poolName, itemName, weight)).send(sender);
            }

            public List<String> addItemCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                switch (arguments.remains()) {
                    case 1:
                        s.addAll(ItemPoolManager.getInstance().getPoolNames());
                        break;
                    case 2:
                        s.addAll(ItemPoolManager.getInstance().getItemNames());
                        break;
                    case 3:
                        s.add("weight");
                        break;
                }
                return filtered(arguments, s);
            }

            @SubCommand(value = "deleteItem", permission = "sw.command", tabCompleter = "deleteItemCompleter")
            public void onDeleteItem(CommandSender sender, Arguments arguments) {
                String poolName = arguments.nextString();
                String itemName = arguments.nextString();
                ItemPool pool = ItemPoolManager.getInstance().getPool(poolName);
                if (pool == null) {
                    new Message("").append(I18n.format("pool.delete_item.error_no_pool", poolName)).send(sender);
                    return;
                }
                if (!pool.contains(itemName)) {
                    new Message("").append(I18n.format("pool.delete_item.error_no_item", poolName, itemName)).send(sender);
                    return;
                }
                pool.removeItem(itemName);
                new Message("").append(I18n.format("pool.delete_item.success", poolName, itemName)).send(sender);
                ItemPoolManager.getInstance().save();
            }

            public List<String> deleteItemCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                switch (arguments.remains()) {
                    case 1:
                        s.addAll(ItemPoolManager.getInstance().getPoolNames());
                        break;
                    case 2:
                        String poolName = arguments.nextString();
                        ItemPool pool = ItemPoolManager.getInstance().getPool(poolName);
                        if (pool != null) {
                            s.addAll(pool.getItemNames());
                        }
                }
                return filtered(arguments, s);
            }

            @Override
            public String getHelpPrefix() {
                return null;
            }
        }

        public static class ItemCommand extends CommandReceiver {
            /**
             * @param plugin for logging purpose only
             * @param _i18n
             */
            public ItemCommand(Plugin plugin, ILocalizer _i18n) {
                super(plugin, _i18n);
            }

            @SubCommand(value = "define", permission = "sw.command", tabCompleter = "defineCompleter")
            public void onDefine(CommandSender sender, Arguments arguments) {
                String name = arguments.nextString();
                if (!(sender instanceof Player)) {
                    new Message("").append(I18n.format("error.not_player")).send(sender);
                    return;
                }
                ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)) {
                    new Message("").append(I18n.format("error.no_item_in_mainhand")).send(sender);
                    return;
                }
                ItemPoolManager.getInstance().addItem(name, itemInMainHand);
                new Message("").append(I18n.format("pool.item.define.success", name)).send(sender);
            }

            public List<String> defineCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                switch (arguments.remains()) {
                    case 1:
                        s.add("name");
                        break;
                }
                return filtered(arguments, s);
            }

            @SubCommand(value = "remove", permission = "sw.command", tabCompleter = "removeCompleter")
            public void onRemove(CommandSender sender, Arguments arguments) {
                String name = arguments.nextString();
                ItemStack item = ItemPoolManager.getInstance().getItem(name);
                if (item == null) {
                    new Message("").append(I18n.format("pool.item.remove.error_no_item", name)).send(sender);
                    return;
                }
                ItemPoolManager.getInstance().removeItem(name);
                new Message("").append(I18n.format("pool.item.remove.success", name)).send(sender);
            }

            public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                switch (arguments.remains()) {
                    case 1:
                        s.addAll(ItemPoolManager.getInstance().getItemNames());
                        break;
                }
                return filtered(arguments, s);
            }


            @Override
            public String getHelpPrefix() {
                return null;
            }
        }

        public static class RollerCommand extends CommandReceiver {
            /**
             * @param plugin for logging purpose only
             * @param _i18n
             */
            public RollerCommand(Plugin plugin, ILocalizer _i18n) {
                super(plugin, _i18n);
            }

            @SubCommand(value = "define", permission = "sw.command", tabCompleter = "defineCompleter")
            public void onDefine(CommandSender sender, Arguments arguments) {
                if (!(sender instanceof Player)) {
                    new Message("").append(I18n.format("error.not_player")).send(sender);
                    return;
                }
                Block targetBlock = ((Player) sender).getTargetBlock(null, 10);
                if (!(targetBlock.getState() instanceof Chest)) {
                    new Message("").append(I18n.format("roller.define.error_not_targeting_chest")).send(sender);
                    return;
                }

                String name = arguments.nextString();
                String normalPoolName = arguments.nextString();
                String extraPoolName = arguments.nextString();
                int cost = arguments.nextInt();
                int extraCost = arguments.nextInt();
                ItemPoolManager itemPoolManager = ItemPoolManager.getInstance();
                ItemPool normalPool = itemPoolManager.getPool(normalPoolName);
                ItemPool extraPool = itemPoolManager.getPool(extraPoolName);
                if (itemPoolManager.hasPoolChest(name)) {
                    new Message("").append(I18n.format("roller.define.failed_exists", name)).send(sender);
                    return;
                }
                if (itemPoolManager.isPoolChest(targetBlock)) {
                    new Message("").append(I18n.format("roller.define.failed_exists", name)).send(sender);
                    return;
                }
                if (normalPool == null) {
                    new Message("").append(I18n.format("roller.define.pool_not_exist", normalPoolName)).send(sender);
                    return;
                }
                if (extraPool == null) {
                    new Message("").append(I18n.format("roller.define.pool_not_exist", normalPoolName)).send(sender);
                    return;
                }
                itemPoolManager.addChest(name, targetBlock, normalPool, extraPool, cost, extraCost);
                new Message("").append(I18n.format("roller.define.success", name)).send(sender);
            }

            public List<String> defineCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                switch (arguments.remains()) {
                    case 1:
                        s.add("<name>");
                        break;
                    case 2:
                        s.add("<normal_pool>");
                        s.addAll(ItemPoolManager.getInstance().getPoolNames());
                        break;
                    case 3:
                        s.add("<extra_pool>");
                        s.addAll(ItemPoolManager.getInstance().getPoolNames());
                        break;
                    case 4:
                        s.add("<cost>");
                        break;
                    case 5:
                        s.add("<extra_cost>");
                        break;
                }
                return filtered(arguments, s);
            }

            @SubCommand(value = "remove", permission = "sw.command", tabCompleter = "removeCompleter")
            public void onRemove(CommandSender sender, Arguments arguments) {
                if (!(sender instanceof Player)) {
                    new Message("").append(I18n.format("error.not_player")).send(sender);
                    return;
                }
                Block targetBlock = ((Player) sender).getTargetBlock(null, 10);
                BlockState state = targetBlock.getState();
                if (state instanceof Chest && ItemPoolManager.getInstance().isPoolChest(targetBlock)) {
                    String removed = ItemPoolManager.getInstance().removePoolChest(targetBlock);
                    if (removed != null) {
                        new Message("").append(I18n.format("roller.remove.success", removed)).send(sender);
                    } else {
                        new Message("").append(I18n.format("roller.define.error_not_roller_chest")).send(sender);
                    }
                } else {
                    new Message("").append(I18n.format("roller.remove.error_not_targeting_chest")).send(sender);
                }
            }

            public List<String> removeCompleter(CommandSender sender, Arguments arguments) {
                List<String> s = new ArrayList<>();
                return filtered(arguments, s);
            }

            @Override
            public String getHelpPrefix() {
                return null;
            }
        }

        @Override
        public String getHelpPrefix() {
            return null;
        }
    }

    public static class DebugCommand extends CommandReceiver{
        /**
         * @param plugin for logging purpose only
         * @param _i18n
         */
        public DebugCommand(Plugin plugin, ILocalizer _i18n) {
            super(plugin, _i18n);
        }

        @SubCommand(value = "kill", permission = "sw.command")
        public void onKill(CommandSender sender, Arguments arguments){
            String from = arguments.nextString();
            String died = arguments.nextString();
            if (sender instanceof Player) {
                World world = ((Player) sender).getWorld();
                Server server = sender.getServer();
                Player diedPlayer = server.getPlayer(died);
                Player fromPlayer = server.getPlayer(from);
                ItemStack itemInMainHand = ((Player) sender).getInventory().getItemInMainHand();
                Optional<AbstractSnowball> item = ItemManager.getInstance().getItem(itemInMainHand);
                if (item.isPresent()) {
                    ItemStack item1 = item.get().getItem();
                    HealthUi.getInstance().damage(diedPlayer, 100, fromPlayer, item1);
                }
            }
        }

        @SubCommand(value = "goldenwind", permission = "sw.command")
        public void onGoldenWind(CommandSender sender, Arguments arguments){
            if (sender instanceof Player) {
                Location location = ((Player) sender).getLocation();
                File file = new File(SnowarsPlugin.plugin.getDataFolder(), "golden_wind.trak");
                try {
                    IMusicSheet iMusicSheet = MusicLoader.loadFromFile(file);
                    IMusicTask play = new MusicPlayer().play(iMusicSheet, (Entity) sender);
                    play.play(SnowarsPlugin.plugin);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public String getHelpPrefix() {
            return null;
        }
    }
}
