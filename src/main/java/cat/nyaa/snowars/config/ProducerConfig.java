package cat.nyaa.snowars.config;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

public class ProducerConfig implements ISerializable {
    @Serializable
    public double capacity = 128;
    @Serializable
    public double current = 0;
    @Serializable
    public double snowballPerSec = 1;
    @Serializable
    public String item;

    public void setItem(ItemStack itemStack) {
        item = ItemStackUtils.itemToBase64(itemStack);
    }

    public ItemStack getItem() {
        return ItemStackUtils.itemFromBase64(item);
    }
}
