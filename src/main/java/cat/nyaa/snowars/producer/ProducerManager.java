package cat.nyaa.snowars.producer;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.snowars.I18n;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.ProducerConfig;
import cat.nyaa.snowars.event.TickEvent;
import cat.nyaa.snowars.event.TickTask;
import cat.nyaa.snowars.event.Ticker;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ProducerManager extends FileConfigure {
    private static ProducerManager INSTANCE;

    private boolean stopped = true;
    private ProducerTask producerTask = new ProducerTask(tickEvent -> stopped);

    @Serializable
    private Map<String, Producer> nbtMap;

    private Map<String, BonusSocks> socksMap;
    @Serializable
    Map<String, ProducerConfig> configMap;

    public Producer summonProducer(Location location, Team team, ProducerConfig producerConfig){
        World world = location.getWorld();
        if (world == null)return null;
        ArmorStand spawn = world.spawn(location, ArmorStand.class, armorStand -> {
            armorStand.setHelmet(new ItemStack(Material.SNOWBALL));
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
        });
        Producer producer = new Producer(spawn, team, producerConfig);
        register(producer);
        return producer;
    }

    public Collection<? extends String> getConfigNames() {
        return configMap.keySet();
    }

    public ProducerConfig define(String name, double capacity, double current, double produceSpeed, ItemStack is){
        if (configMap.containsKey(name)){
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

    public ProducerConfig getConfig(String name){
        return configMap.get(name);
    }

    private void register(Producer producer) {
        nbtMap.put(producer.uuid, producer);
    }

    private ProducerManager(){
        nbtMap = new HashMap<>();
        configMap = new LinkedHashMap<>();
        socksMap = new LinkedHashMap<>();
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
        if(INSTANCE == null){
            synchronized (ProducerManager.class){
                if (INSTANCE == null){
                    INSTANCE = new ProducerManager();
                }
            }
        }
        return INSTANCE;
    }

    public boolean isProducer(Entity rightClicked) {
        return nbtMap.containsKey(rightClicked.getUniqueId().toString()) || socksMap.containsKey(rightClicked.getUniqueId().toString());
    }

    public Producer getProducer(Entity rightClicked) {
        Producer producer = nbtMap.get(rightClicked.getUniqueId().toString());
        if (producer == null){
            producer = socksMap.get(rightClicked.getUniqueId().toString());
        }
        return producer;
    }

    public void start() {
        stopped = false;
        Ticker.getInstance().register(producerTask);
    }

    public void stop() {
        stopped = true;
        socksMap.values().forEach(bonusSocks -> bonusSocks.remove());
        socksMap.clear();
    }

    public void destroy(Entity toDestroy) {
        Producer remove = nbtMap.remove(toDestroy.getUniqueId().toString());
        remove.producerEntity.remove();
    }

    public void removeLater(String uuid) {
        new BukkitRunnable(){
            @Override
            public void run() {
                nbtMap.remove(uuid);
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
        BonusSocks bonusSocks = new BonusSocks(spawn, null, config);
        socksMap.put(bonusSocks.uuid, bonusSocks);
        return bonusSocks;
    }

    public class ProducerTask extends TickTask{
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
