package cat.nyaa.snowars.utils;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.RegionConfig;
import cat.nyaa.snowars.producer.ProducerManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegionManager extends FileConfigure {
    private static RegionManager INSTANCE;

    private RegionManager() {
        regionMap = new LinkedHashMap<>();
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
        RegionConfig regionConfig = regionMap.get(regionName);
        regionConfig.team = team.getName();
    }

    public Collection<? extends String> getRegionNames() {
        return regionMap.keySet();
    }

    public Collection<RegionConfig> getRegions() {
        return regionMap.values();
    }
}
