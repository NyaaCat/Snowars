package cat.nyaa.snowars;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class ScoreManager {
    private static final ScoreManager INSTANCE = new ScoreManager();

    private Map<Entity, Integer> scoreMap;

    private ScoreManager(){
        scoreMap = new HashMap<>();
    }

    public static ScoreManager getInstance(){
        return INSTANCE;
    }

    public void addFor(Entity from, int hitScore) {
        Integer score = scoreMap.computeIfAbsent(from, i -> 0);
        scoreMap.put(from, score + hitScore);
    }
}
