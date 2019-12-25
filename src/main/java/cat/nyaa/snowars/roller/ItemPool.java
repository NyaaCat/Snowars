package cat.nyaa.snowars.roller;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemPool implements ISerializable {
    @Serializable
    public Map<String, Integer> pool = new HashMap<>();

    public ItemPool(){}

    public ItemStack rollItem(){
        String result = Utils.weightedRandomPick(pool);
        ItemStack item = ItemPoolManager.getInstance().getItem(result);
        return item;
    }

    public Collection<? extends String> getItemNames() {
        return pool.keySet();
    }

    public boolean contains(String itemName) {
        return pool.containsKey(itemName);
    }

    public void removeItem(String itemName) {
        pool.remove(itemName);
    }

    public void addItem(String itemName, int weight) {
        pool.put(itemName, weight);
    }
}
