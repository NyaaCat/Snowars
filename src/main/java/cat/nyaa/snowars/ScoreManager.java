package cat.nyaa.snowars;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.snowars.ui.HealthUi;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ScoreManager extends FileConfigure {
    private static final ScoreManager INSTANCE = new ScoreManager();

    @Serializable
    private Map<String, Double> scoreMap;
    @Serializable
    private Map<String, Double> teamScoreMap;
    private List<Entity> goldExperienced;

    private ScoreManager() {
        scoreMap = new HashMap<>();
        goldExperienced = new ArrayList<>();
        teamScoreMap = new HashMap<>();
    }

    @Override
    protected String getFileName() {
        return "score.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return SnowarsPlugin.plugin;
    }

    public static ScoreManager getInstance() {
        return INSTANCE;
    }

    public void addFor(Entity from, double hitScore) {
        Team team = Utils.getTeam(from);
        String uuid = getEntityKey(from);
        String teamName = getTeamKey(team);

        double score = scoreMap.computeIfAbsent(uuid, i -> 0d);
        double teamScore = teamScoreMap.computeIfAbsent(teamName, team1 -> 0d);

        hitScore = calcFinalScore(from, hitScore);

        scoreMap.put(uuid, score + hitScore);
        teamScoreMap.put(teamName, teamScore + hitScore);
    }

    private String getTeamKey(Team team) {
        return team.getName();
    }

    private double calcFinalScore(Entity from, double hitScore) {
        double score = hitScore;
        if (goldExperienced.contains(from)) {
            if (hitScore > 0) {
                score = 2 * hitScore;
            } else {
                score = 3 * hitScore;
            }
        }
        return score;
    }

    public void subtractFor(Entity entity, int subtractScore) {

    }

    public void goldExperienced(LivingEntity from, int duration) {
        goldExperienced.add(from);
        new BukkitRunnable() {
            @Override
            public void run() {
                goldExperienced.remove(from);
            }
        }.runTaskLater(SnowarsPlugin.plugin, duration);
    }

    public double getScore(Entity player) {
        return scoreMap.computeIfAbsent(getEntityKey(player), player1 -> 0d);
    }

    public double getTeamScore(Player poll) {
        Team team = Utils.getTeam(poll);
        if (team == null) return 0;
        return teamScoreMap.computeIfAbsent(getTeamKey(team), team1 -> 0d);
    }

    public void damage(Entity entity, double damage) {
        HealthUi.getInstance().damage(entity, damage);
    }

    public Collection<? extends String> getPlayers() {
        Set<String> players = new HashSet<>();
        scoreMap.keySet().stream()
                .forEach(s -> {
                    OfflinePlayer offlinePlayer = SnowarsPlugin.plugin.getServer().getOfflinePlayer(UUID.fromString(s));
                    if (offlinePlayer != null) {
                        players.add(offlinePlayer.getName());
                    }
                });
        return players;
    }

    public void clearScore(Player player) {
        String key = getEntityKey(player);
        double val = scoreMap.computeIfAbsent(key, s -> 0d);
        scoreMap.put(key, 0d);
        Team team = Utils.getTeam(player);
        String teamKey = getTeamKey(team);
        if (teamScoreMap.containsKey(teamKey)) {
            teamScoreMap.put(teamKey, teamScoreMap.get(teamKey) - val);
        }
    }

    private String getEntityKey(Entity player) {
        return player.getUniqueId().toString();
    }


    public void clearScore(Team team) {
        Server server = SnowarsPlugin.plugin.getServer();
        String teamName = getTeamKey(team);
        Set<String> entries = team.getEntries();
        double teamScore = teamScoreMap.computeIfAbsent(teamName, s -> 0d);
        teamScoreMap.put(teamName, 0d);
        HashMap<String, OfflinePlayer> playerHashMap = new HashMap<>();

        entries.forEach(s -> {
            if (scoreMap.containsKey(s)) {
                scoreMap.put(s, 0d);
            } else {
                if (playerHashMap.containsKey(s)) {
                    scoreMap.put(playerHashMap.get(s).getUniqueId().toString(), 0d);
                } else {
                    Arrays.stream(server.getOfflinePlayers())
                            .forEach(offlinePlayer -> playerHashMap.put(offlinePlayer.getName(), offlinePlayer));
                    try {
                        Entity entity = server.getEntity(UUID.fromString(s));
                        if (entity != null) {
                            double origin = scoreMap.computeIfAbsent(s, s1 -> 0d);
                            scoreMap.put(s, 0d);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    public void clearAll() {
        scoreMap.clear();
        teamScoreMap.clear();
    }

    public double getScore(String s) {
        Server server = SnowarsPlugin.plugin.getServer();
        HashMap<String, OfflinePlayer> playerHashMap = new HashMap<>();
        double score = 0;
        if (scoreMap.containsKey(s)) {
            score = scoreMap.get(s);
        } else {
            Arrays.stream(server.getOfflinePlayers())
                    .forEach(offlinePlayer -> playerHashMap.put(offlinePlayer.getName(), offlinePlayer));
            if (playerHashMap.containsKey(s)) {
                score = scoreMap.get(playerHashMap.get(s).getUniqueId().toString());
            }
        }
        return score;
    }

    public void setScore(Entity from, double v) {
        double originalScore = getScore(from);
        String entityKey = getEntityKey(from);
        scoreMap.put(entityKey, v);
        Team team = Utils.getTeam(from);
        if (team != null) {
            double diff = v - originalScore;
            Double originalTeamScore = teamScoreMap.computeIfAbsent(getTeamKey(team), s -> 0d);
            teamScoreMap.put(getTeamKey(team), originalTeamScore + diff);
        }
    }
}
