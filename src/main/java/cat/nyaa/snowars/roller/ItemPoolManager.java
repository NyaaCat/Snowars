package cat.nyaa.snowars.roller;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.producer.ProducerManager;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemPoolManager extends FileConfigure {
    private static ItemPoolManager INSTANCE;

    public static ItemPoolManager getInstance(){
        if(INSTANCE == null){
            synchronized (ProducerManager.class){
                if (INSTANCE == null){
                    INSTANCE = new ItemPoolManager();
                }
            }
        }
        return INSTANCE;
    }

    private ItemPoolManager(){
    }

    @Serializable
    Map<String, String> itemMap = new LinkedHashMap<>();
    @Serializable
    Map<String, ItemPool> itemPoolMap = new LinkedHashMap<>();
    @Serializable
    Map<String, PresentChest> presentChestMap = new LinkedHashMap<>();
    Map<Block, PresentChest> inventoryMap = new LinkedHashMap<>();

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        presentChestMap.values().forEach(presentChest -> {
            Inventory chestInventory = presentChest.getChestInventory();
            if (chestInventory != null) {
                inventoryMap.put(presentChest.getBlock(), presentChest);
            }
        });
    }

    public void addChest(String name, Block chestBlock, ItemPool itemPool, ItemPool extraPool, int cost, int extraCost){
        BlockState state = chestBlock.getState();
        if (state instanceof Chest){
            Inventory blockInventory = ((Chest) state).getBlockInventory();
            PresentChest presentChest = new PresentChest(chestBlock, itemPool, extraPool, cost, extraCost);
            presentChestMap.put(name, presentChest);
            inventoryMap.put(presentChest.getBlock(), presentChest);
        }
        save();
    }

    public void addItem(String name, ItemStack itemStack){
        itemMap.put(name, ItemStackUtils.itemToBase64(itemStack));
        save();
    }

    public ItemStack getItem(String name){
        if (name == null)return null;
        String b64 = itemMap.get(name);
        if (b64 == null)return null;
        return ItemStackUtils.itemFromBase64(b64);
    }

    @Override
    protected String getFileName() {
        return "pollItems.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return SnowarsPlugin.plugin;
    }

    public String getPoolName(ItemPool itemPool) {
        Map.Entry<String, ItemPool> stringItemPoolEntry = itemPoolMap.entrySet().stream().filter(entry -> entry.getValue() == itemPool).findAny().orElse(null);
        return stringItemPoolEntry == null ? "" : stringItemPoolEntry.getKey();
    }

    public ItemPool getPool(String itemPoolName) {
        return itemPoolMap.get(itemPoolName);
    }

    public boolean isPoolChest(Block block) {
        return inventoryMap.containsKey(block) && inventoryMap.get(block).isValid();
    }

    public PresentChest getChest(Block block) {
        return inventoryMap.get(block);
    }

    public void createPool(String name) {
        ItemPool pool = new ItemPool();
        itemPoolMap.put(name, pool);
        save();
    }

    public void removePool(String name) {
        itemPoolMap.remove(name);
        save();
    }

    public Collection<? extends String> getPoolNames() {
        return itemPoolMap.keySet();
    }

    public void removeItem(String name) {
        itemMap.remove(name);
        itemPoolMap.values().forEach(itemPool -> {
            itemPool.pool.remove(name);
        });
        save();
    }

    public Collection<? extends String> getItemNames() {
        return itemMap.keySet();
    }

    public String removePoolChest(Block block) {
        PresentChest presentChest = inventoryMap.get(block);
        if (presentChest == null)return null;
        Map.Entry<String, PresentChest> stringPresentChestEntry = presentChestMap.entrySet().stream().filter(entry -> entry.getValue() == presentChest)
                .findAny().orElse(null);
        if (stringPresentChestEntry != null) {
            String key = stringPresentChestEntry.getKey();
            presentChestMap.remove(key);
            presentChest.removeDisplay();
            inventoryMap.remove(block);
            return key;
        }
        return null;
    }

    public boolean hasPoolChest(String name) {
        return presentChestMap.containsKey(name);
    }
}
