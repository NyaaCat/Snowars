package cat.nyaa.snowars.producer;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.snowars.I18n;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.ProducerConfig;
import cat.nyaa.snowars.event.TickEvent;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.event.Ticker;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import static cat.nyaa.snowars.utils.Utils.colored;

public class ProducerManager extends FileConfigure {
    private static ProducerManager INSTANCE;

    private boolean stopped = true;
    private ProducerTask producerTask = new ProducerTask(tickEvent -> stopped);

    @Serializable
    private Map<String, Producer> nbtMap;

    private Map<String, BonusSocks> socksMap;

    @Serializable
    Map<String, ProducerConfig> configMap;

    public Producer summonProducer(Location location, ProducerConfig producerConfig) {
        LivingEntity spawn1 = summonEntity(location, null);
//        ArmorStand spawn = world.spawn(location, ArmorStand.class, armorStand -> {
//            armorStand.setHelmet(new ItemStack(Material.SNOWBALL));
//            armorStand.setInvulnerable(true);
//            armorStand.setCustomNameVisible(true);
//            armorStand.setVisible(false);
//            armorStand.setHelmet(producerHead.clone());
//            armorStand.setChestplate(producerChestPlate.clone());
//            armorStand.setLeggings(producerLeggings.clone());
//            armorStand.setBoots(producerBoots.clone());
//        });
        Producer producer = new Producer(spawn1, producerConfig);
        register(producer);
        return producer;
    }

    public LivingEntity summonEntity(Location location, String uid) {
        World world = location.getWorld();
        if (world == null) return null;
        Snowman spawn1 = world.spawn(location, Snowman.class, snowman -> {
            snowman.setAI(false);
            snowman.setCustomNameVisible(true);
            snowman.setInvulnerable(true);
        });
        return spawn1;
    }

    public Collection<? extends String> getConfigNames() {
        return configMap.keySet();
    }

    public ProducerConfig define(String name, double capacity, double current, double produceSpeed, ItemStack is) {
        if (configMap.containsKey(name)) {
            return configMap.get(name);
        }
        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.capacity = capacity;
        producerConfig.current = current;
        producerConfig.snowballPerSec = produceSpeed;
        producerConfig.setItem(is);
        configMap.put(name, producerConfig);
        save();
        return producerConfig;
    }

    public ProducerConfig getConfig(String name) {
        return configMap.get(name);
    }

    private void register(Producer producer) {
        nbtMap.put(producer.uuid, producer);
    }

    private ProducerManager() {
        nbtMap = new HashMap<>();
        configMap = new LinkedHashMap<>();
        socksMap = new LinkedHashMap<>();
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        nbtMap.forEach((s,producer) ->{
            producer.uuidKey = s;
        });
    }

    @Override
    protected String getFileName() {
        return "producer.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return SnowarsPlugin.plugin;
    }

    public static ProducerManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ProducerManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ProducerManager();
                }
            }
        }
        return INSTANCE;
    }

    public boolean isProducer(Entity rightClicked) {
        return rightClicked.getScoreboardTags().contains("snowar_producer") || nbtMap.containsKey(rightClicked.getUniqueId().toString()) || socksMap.containsKey(rightClicked.getUniqueId().toString());
    }

    public Producer getProducer(Entity rightClicked) {
        Producer producer = nbtMap.get(rightClicked.getUniqueId().toString());
        if (producer == null){
            String uidStr = rightClicked.getScoreboardTags().stream().filter(s -> s.startsWith("snowar_uid")).findAny().orElse(null);
            if (uidStr != null){
                String uid = uidStr.split(":")[1];
                producer = nbtMap.get(uid);
            }
        }
        if (producer == null) {
            producer = socksMap.get(rightClicked.getUniqueId().toString());
        }
        return producer;
    }

    public void start() {
        if (!stopped) {
            stop();
        }
        stopped = false;
        Ticker.getInstance().register(producerTask);
    }

    public void stop() {
        stopped = true;
        socksMap.values().forEach(bonusSocks -> bonusSocks.remove());
        socksMap.clear();
    }

    public void destroy(Entity toDestroy) {
        String uidStr = toDestroy.getScoreboardTags().stream().filter(s -> s.startsWith("snowar_uid")).findAny().orElse(null);
        if (uidStr != null){
            String uid = uidStr.split(":")[1];
            Producer remove = nbtMap.remove(uid);
            remove.producerEntity.remove();
        }
    }

    public void removeLater(String uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                nbtMap.remove(uuid);
                socksMap.remove(uuid);
            }
        }.runTaskLater(SnowarsPlugin.plugin, 1);
    }

    private static ItemStack sockLegging = new ItemStack(Material.LEATHER_LEGGINGS);
    private static ItemStack sockBoots = new ItemStack(Material.LEATHER_BOOTS);

    static {
        LeatherArmorMeta leggingMeta = (LeatherArmorMeta) sockLegging.getItemMeta();
        leggingMeta.setColor(Color.WHITE);
        sockLegging.setItemMeta(leggingMeta);
        LeatherArmorMeta bootItemMeta = (LeatherArmorMeta) sockBoots.getItemMeta();
        bootItemMeta.setColor(Color.RED);
        sockBoots.setItemMeta(bootItemMeta);
    }

    public BonusSocks summonBonusSocks(Location location, ProducerConfig config) {
        ArmorStand spawn = location.getWorld().spawn(location, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', I18n.format("bonus_socks")));
            armorStand.setCustomNameVisible(true);
            armorStand.setSmall(true);
            armorStand.setInvulnerable(true);
            armorStand.setBasePlate(false);
            armorStand.setLeggings(sockLegging.clone());
            armorStand.setBoots(sockBoots.clone());
            armorStand.addScoreboardTag("bonus_socks");
        });
        BonusSocks bonusSocks = new BonusSocks(spawn, config);
        socksMap.put(bonusSocks.uuid, bonusSocks);
        return bonusSocks;
    }

    public void remove(String name) {
        configMap.remove(name);
    }

    public void summonBonusChicken(Location location) {
        World world = location.getWorld();
        Chicken spawn = world.spawn(location, Chicken.class, chicken -> {
            chicken.setCustomNameVisible(true);
            chicken.setCustomName(colored(I18n.format("lucky_chicken")));
            chicken.addScoreboardTag("lucky_entity");
        });
        Utils.removeLater(spawn, 6000);
    }

    public void summonBonusBunny(Location location) {
        World world = location.getWorld();
        Rabbit spawn = world.spawn(location, Rabbit.class, chicken -> {
            chicken.setCustomNameVisible(true);
            chicken.setCustomName(colored(I18n.format("lucky_bunny")));
            chicken.addScoreboardTag("lucky_entity");
        });
        Utils.removeLater(spawn, 6000);
    }

    public Collection<Producer> getProducers() {
        return nbtMap.values();
    }

    public void clearSocks() {
        socksMap.clear();
    }

    public class ProducerTask extends TickTask {
        public ProducerTask(Predicate<TickEvent> shouldRemove) {
            super(shouldRemove);
        }

        @Override
        public void run(int ticked) {
            nbtMap.values().forEach(producer -> producer.tick(ticked));
            socksMap.values().forEach(bonusSocks -> bonusSocks.tick(ticked));
        }
    }
}
