package cat.nyaa.snowars.item;

import cat.nyaa.nyaacore.utils.ClassPathUtils;
import cat.nyaa.snowars.Configurations;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.config.ItemConfigs;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemManager {
    private static final ItemManager INSTANCE = new ItemManager();
    public static NamespacedKey SNOWBALL_ITEM= new NamespacedKey(SnowarsPlugin.plugin, "snowball_item");

    private Map<String, AbstractSnowball> snowballMap;
    private Map<String, ItemStack> itemStackMap;
    private Map<String, Class<? extends AbstractSnowball>> behaviorMap;
    private Map<AbstractSnowball, String> snowballItemNameMap;

    private ItemManager() {
        snowballMap = new HashMap<>();
        itemStackMap = new HashMap<>();
        behaviorMap = new HashMap<>();
        snowballItemNameMap = new HashMap<>();
    }

    public static ItemManager getInstance() {
        return INSTANCE;
    }

    public static void load(Configurations configurations) {
        buildBehaviors();
        Map<String, AbstractSnowball> snowballMap = getInstance().snowballMap;
        Map<String, ItemStack> itemStackMap = getInstance().itemStackMap;
        Map<AbstractSnowball, String> snowballItemNameMap = getInstance().snowballItemNameMap;
        snowballMap.clear();
        itemStackMap.clear();
        configurations.itemConfigs.forEach((s, ic) -> {
            ic.id = s;
            ItemStack itemStack = itemFrom(ic);
            AbstractSnowball snowball = null;
            Class<? extends AbstractSnowball> aClass = getInstance().behaviorMap.get(ic.behavior);
            if (aClass == null)return;
            try{
                snowball = aClass.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
            snowballMap.put(s, snowball);
            itemStackMap.put(s, itemStack);
            snowballItemNameMap.put(snowball, s);
        });
    }

    private static void buildBehaviors() {
        Map<String, Class<? extends AbstractSnowball>> behaviorMap = getInstance().behaviorMap;
        behaviorMap.clear();
        Class<? extends AbstractSnowball>[] classes = ClassPathUtils.scanSubclasses(SnowarsPlugin.plugin, "cat.nyaa.snowars.item.impl", AbstractSnowball.class);
        if (classes.length > 0) {
            for (Class<? extends AbstractSnowball> aClass : classes) {
                String simpleName = aClass.getSimpleName();
                String substring = simpleName.substring(8).toLowerCase();
                behaviorMap.put(substring, aClass);
            }
        }
    }

    private static ItemStack itemFrom(ItemConfigs ic) {
        ItemStack itemStack = new ItemStack(ic.material);
        itemStack.setAmount(1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', ic.name));
            itemMeta.getPersistentDataContainer().set(SNOWBALL_ITEM, PersistentDataType.STRING, ic.id);
            ic.lore = ic.lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
            itemMeta.setLore(ic.lore);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public Optional<AbstractSnowball> getItem(ItemStack is) {
        ItemMeta itemMeta = is.getItemMeta();
        if (itemMeta == null) return Optional.empty();
        String itemID = itemMeta.getPersistentDataContainer().get(SNOWBALL_ITEM, PersistentDataType.STRING);
        AbstractSnowball abstractSnowball = snowballMap.get(itemID);
        return Optional.ofNullable(abstractSnowball);
    }

    public ItemStack getItem(String string) {
        return itemStackMap.get(string);
    }

    public Collection<? extends String> getItemNames() {
        return itemStackMap.keySet();
    }

    public ItemStack getItem(AbstractSnowball abstractSnowball) {
        String s = snowballItemNameMap.get(abstractSnowball);
        return s == null? null : itemStackMap.get(s);
    }
}
