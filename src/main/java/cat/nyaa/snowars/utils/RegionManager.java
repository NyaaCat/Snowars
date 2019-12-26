package cat.nyaa.snowars.utils;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.RegionConfig;
import cat.nyaa.snowars.producer.ProducerManager;
import cat.nyaa.snowars.ui.HealthUi;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegionManager extends FileConfigure {
    private static RegionManager INSTANCE;

    private RegionManager() {
        regionMap = new LinkedHashMap<>();
        teamRegionMap = new LinkedHashMap<>();
    }

    public static RegionManager getInstance(){
        if(INSTANCE == null){
            synchronized (ProducerManager.class){
                if (INSTANCE == null){
                    INSTANCE = new RegionManager();
                }
            }
        }
        return INSTANCE;
    }

    @Serializable
    Map<String, RegionConfig> regionMap;

    @Serializable
    Map<String, String> teamRegionMap;

    public void addRegion(RegionConfig.Region region, String name) {
        RegionConfig config = new RegionConfig(name, region);
        regionMap.put(name, config);
        save();
    }

    public void removeRegion(String name){
        regionMap.remove(name);
        save();
    }

    @Override
    protected String getFileName() {
        return "regions.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return SnowarsPlugin.plugin;
    }

    public boolean contains(String name) {
        return regionMap.containsKey(name);
    }

    public RegionConfig getRegion(String regionName) {
        return regionMap.get(regionName);
    }

    public void setTeam(String regionName, Team team) {
        teamRegionMap.put(team.getName(), regionName);
        save();
    }

    public Collection<? extends String> getRegionNames() {
        return regionMap.keySet();
    }

    public Collection<RegionConfig> getRegions() {
        return regionMap.values();
    }

    public RegionConfig getTeamRegion(Team team) {
        return regionMap.get(teamRegionMap.get(team.getName()));
    }

    public List<String> getRegionsForLocation(Location location) {
        return regionMap.entrySet().stream()
                .filter(entry -> {
                    RegionConfig regionConfig1 = entry.getValue();
                    return regionConfig1.region != null && regionConfig1.region.contains(location);
                })
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }

    public boolean isInvalidRegion(Player player, String regionsForLocation) {
        Team team = Utils.getTeam(player);
        if (team == null)return false;
        return teamRegionMap.entrySet().stream()
                .anyMatch(entry -> entry.getValue().equals(regionsForLocation) && !entry.getKey().equals(team.getName()));
    }
}
