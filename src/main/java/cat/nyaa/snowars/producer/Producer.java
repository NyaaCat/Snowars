package cat.nyaa.snowars.producer;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.snowars.I18n;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.ProducerConfig;
import cat.nyaa.snowars.item.ItemManager;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.UUID;
import java.util.logging.Level;

public class Producer implements ISerializable {
    @Serializable
    String uuid;
    @Serializable
    double capacity = 128;
    @Serializable
    double current = 0;
    @Serializable
    double snowballPerSec = 1;
    @Serializable
    String item = "";

    private ItemStack itemCache = null;
    LivingEntity producerEntity;

    public Producer(LivingEntity spawn, ProducerConfig producerConfig) {
        producerEntity = spawn;
        uuid = spawn.getUniqueId().toString();
        updateConfig(producerConfig);
        updateName();
    }

    public Producer() {
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        try {
            producerEntity = (LivingEntity) SnowarsPlugin.plugin.getServer().getEntity(UUID.fromString(uuid));
        } catch (Exception e) {
            SnowarsPlugin.plugin.getLogger().log(Level.INFO, String.format("no entity found for producer, removing."));
            ProducerManager.getInstance().removeLater(uuid);
        }
    }

    public void updateConfig(ProducerConfig producerConfig) {
        this.capacity = producerConfig.capacity;
        this.current = producerConfig.current;
        this.snowballPerSec = producerConfig.snowballPerSec;
        this.item = producerConfig.item;
    }

    protected void updateName() {
        if (producerEntity.isDead()) {
            ProducerManager.getInstance().removeLater(uuid);
            return;
        }
        producerEntity.setCustomName(Utils.colored(getName()));
    }

    protected String getName() {
        return I18n.format("producer.name", ((int) Math.floor(current)), ((int) Math.floor(capacity)));
    }

    public void tick(int tick) {
        double magnification = SnowarsPlugin.plugin.configurations.magnification;
        if (producerEntity == null){
            ProducerManager.getInstance().removeLater(uuid);
            return;
        }
        if (producerEntity.getWorld().hasStorm()) {
            magnification += 1;
        }
        double product =  (current + magnification *(snowballPerSec / 20));
        current = Math.min(product, capacity);
        if (tick % 5 == 0) {
            updateName();
        }
    }

    boolean inCooldown = false;

    public void onClick(Player clicked) {
        int toTake = (int) Math.floor(current);
        ItemStack normal = null;
        if (this.itemCache == null) {
            try {
                itemCache = ItemStackUtils.itemFromBase64(item);
            } catch (Exception e) {
                itemCache = ItemManager.getInstance().getItem("normal");
            }
        }
        normal = itemCache;
        int maxStackSize = normal.getMaxStackSize();
        while ((toTake -= maxStackSize) > 0) {
            take(clicked, normal, maxStackSize);
        }
        toTake += maxStackSize;
        take(clicked, normal, toTake);
        if (!inCooldown) {
            inCooldown = true;
            Location location = producerEntity.getLocation();
            World world = producerEntity.getWorld();
            world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 5, 1);
            world.spawnParticle(Particle.VILLAGER_HAPPY, location, 20, 0.5, 0.5, 0.5, 0.1, null);
            new BukkitRunnable() {
                @Override
                public void run() {
                    inCooldown = false;
                }
            }.runTaskLater(SnowarsPlugin.plugin, 20);
        }
        updateName();
    }

    private void take(Player clicked, ItemStack normal, int amount) {
        ItemStack clone = normal.clone();
        clone.setAmount(amount);
        if (InventoryUtils.hasEnoughSpace(clicked, normal, amount)) {
            InventoryUtils.addItem(clicked, clone);
        } else {
            clicked.getWorld().dropItem(clicked.getLocation(), clone);
        }
        current -= amount;
    }
}
