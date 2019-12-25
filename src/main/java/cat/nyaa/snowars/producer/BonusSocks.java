package cat.nyaa.snowars.producer;

import cat.nyaa.snowars.I18n;
import cat.nyaa.snowars.config.ProducerConfig;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class BonusSocks extends Producer{

    public BonusSocks(LivingEntity spawn, Team team, ProducerConfig producerConfig) {
        super(spawn, team, producerConfig);
    }

    public BonusSocks() {
        super();
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        super.deserialize(config);
    }

    @Override
    public void tick(int tick) {
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
