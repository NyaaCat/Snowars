package cat.nyaa.snowars.item;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SnowballManager {
    public static final SnowballManager INSTANCE = new SnowballManager();

    private Map<UUID, SnowballHandler> snowballHandlerMap;

    private SnowballManager(){
        snowballHandlerMap = new HashMap<>();
    }

    public static SnowballManager getInstance(){
        return INSTANCE;
    }

    public void register(Entity projectile, SnowballHandler snowballHandler){
        snowballHandlerMap.put(projectile.getUniqueId(), snowballHandler);
    }

    public Optional<SnowballHandler> get(Entity entity){
        return Optional.ofNullable(snowballHandlerMap.get(entity.getUniqueId()));
    }

    public void remove(Projectile entity) {
        snowballHandlerMap.remove(entity.getUniqueId());
    }
}
