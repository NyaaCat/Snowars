package cat.nyaa.snowars.producer;

import cat.nyaa.snowars.I18n;
import cat.nyaa.snowars.config.ProducerConfig;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class BonusSocks extends Producer{

    public BonusSocks(LivingEntity spawn, ProducerConfig producerConfig) {
        super(spawn, producerConfig);
    }

    public BonusSocks() {
        super();
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        super.deserialize(config);
    }


    int spawnedTick = 0;

    @Override
    public void tick(int tick) {
        if (spawnedTick == 0) spawnedTick = tick;
        if (spawnedTick > 6000){
            producerEntity.remove();
            ProducerManager.getInstance().removeLater(uuid);
        }
        Location eyeLocation = producerEntity.getLocation();
        float pitch = eyeLocation.getPitch();
        float yaw = eyeLocation.getYaw();
        Location clone = eyeLocation.clone();
        clone.setYaw(yaw+5);
        producerEntity.teleport(clone);
    }

    @Override
    public void onClick(Player clicked) {
        super.onClick(clicked);
        Location location = producerEntity.getLocation();
        World world = producerEntity.getWorld();
        world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 5, 1);
        world.spawnParticle(Particle.CLOUD, location, 50, 0.5, 0.5, 0.5, 0.1, null);
        producerEntity.remove();
        ProducerManager.getInstance().removeLater(uuid);
    }

    @Override
    protected void updateName() {
//        super.updateName();
    }

    @Override
    protected String getName() {
        return I18n.format("socks.name");
    }

    public void remove() {
        producerEntity.remove();
    }
}
