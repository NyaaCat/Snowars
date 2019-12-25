package cat.nyaa.snowars.roller;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.snowars.I18n;
import cat.nyaa.snowars.SnowarsPlugin;
import cat.nyaa.snowars.item.AbstractSnowball;
import cat.nyaa.snowars.item.ItemManager;
import cat.nyaa.snowars.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PresentChest implements ISerializable {
    private Block block;
    private ItemPool itemPool;
    private ItemPool extraPool;
    private Inventory chestInventory;
    private Entity messageDisplay;
    private String errorMessage = "";

    @Serializable
    int blockX;
    @Serializable
    int blockY;
    @Serializable
    int blockZ;
    @Serializable
    String blockWorld = "";
    @Serializable
    ChestState state = ChestState.READY;
    @Serializable
    String displayUid;
    @Serializable
    int cost = 4;
    @Serializable
    int bonusCost = 16;
    @Serializable
    int costItemAmount = 0;
    @Serializable
    String itemPoolName = "";
    @Serializable
    String extraPoolName = "";

    public PresentChest(Block block, ItemPool itemPool, ItemPool extraPool, int cost, int bonusCost) {
        buildBlockInfo(block);
        summonDisplay();
        this.itemPool = itemPool;
        itemPoolName = ItemPoolManager.getInstance().getPoolName(itemPool);
        this.extraPool = extraPool;
        extraPoolName = ItemPoolManager.getInstance().getPoolName(extraPool);
        this.cost = cost;
        this.bonusCost = bonusCost;
    }

    public PresentChest() {
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        World world = SnowarsPlugin.plugin.getServer().getWorld(blockWorld);
        Block blockAt = world.getBlockAt(blockX, blockY, blockZ);
        buildBlockInfo(blockAt);
        ItemPoolManager instance = ItemPoolManager.getInstance();
        itemPool = instance.getPool(itemPoolName);
        extraPool = instance.getPool(extraPoolName);
        messageDisplay = SnowarsPlugin.plugin.getServer().getEntity(UUID.fromString(displayUid));
        if (messageDisplay == null) {
            summonDisplay();
        }
    }

    private void summonDisplay() {
        messageDisplay = Utils.summonNameDisplay(block.getLocation().add(0.5, 0.8, 0.5));
        displayUid = messageDisplay.getUniqueId().toString();
        messageDisplay.addScoreboardTag("present_message");
        updateDisplay();
    }

    private void buildBlockInfo(Block block) {
        this.block = block;
        this.blockX = block.getX();
        this.blockY = block.getY();
        this.blockZ = block.getZ();
        this.blockWorld = block.getWorld().getName();
        if (block.getState() instanceof Chest) {
            chestInventory = ((Chest) block.getState()).getBlockInventory();
        }
    }

    public void proceedInv() {
        if (chestInventory == null) return;
        if (state == ChestState.FINISHED || state == ChestState.ERROR) {
            onOpen();
            return;
        }
        try {
            if (state == ChestState.READY) {
                ItemManager itemManager = ItemManager.getInstance();
                ItemStack[] storageContents = chestInventory.getStorageContents();
                List<Integer> normalIndexes = new ArrayList<>();

                int amount = 0;
                for (int i = 0; i < storageContents.length; i++) {
                    ItemStack storageContent = storageContents[i];
                    if (storageContent == null || storageContent.getType().equals(Material.AIR)) continue;
                    Optional<AbstractSnowball> item = itemManager.getItem(storageContent);
                    if (item.isPresent()) {
                        if (item.get().isNormal()) {
                            normalIndexes.add(i);
                            amount += storageContent.getAmount();
                        }
                    }
                }
                int minCost = cost;
                if (amount > minCost) {
                    for (int i = 0; i < normalIndexes.size(); i++) {
                        storageContents[normalIndexes.get(i)] = new ItemStack(Material.AIR);
                    }
                    chestInventory.setContents(storageContents);

                    costItemAmount += amount;
                    int extraSpawns = costItemAmount / bonusCost;
                    costItemAmount = costItemAmount % bonusCost;
                    for (int i = 0; i < extraSpawns; i++) {
                        ItemStack rollResult = extraPool.rollItem();
                        if (rollResult == null) break;
                        addOrDropItem(rollResult);
                    }

                    while (amount > minCost) {
                        ItemStack rollResult = itemPool.rollItem();
                        if (rollResult == null) {
                            break;
                        }
                        amount -= cost;
                        addOrDropItem(rollResult);
                    }
                    state = ChestState.FINISHED;
                }
                else if (amount != 0){
                    state = ChestState.ERROR;
                    errorMessage = "pool.error.not_enough";
                }else {
                    state = ChestState.READY;
                }
            }
        } catch (Exception e) {
            state = ChestState.ERROR;
            errorMessage = "pool.error.unknown";
        }
        updateDisplay();
    }

    private void updateDisplay() {
        if (messageDisplay != null) {
            String message = messageDisplay.getCustomName();
            switch (state) {
                case READY:
                    message = I18n.format("pool.ready");
                    break;
                case FINISHED:
                    message = I18n.format("pool.finished");
                    break;
                case ERROR:
                    message = I18n.format(errorMessage);
                    break;
            }
            messageDisplay.setCustomName(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private void addOrDropItem(ItemStack is) {
        if (InventoryUtils.hasEnoughSpace(chestInventory, is)) {
            InventoryUtils.addItem(chestInventory, is);
        } else {
            Location loca = block.getLocation().add(0.5, 1.5, 0.5);
            loca.getWorld().dropItem(loca, is);
        }
    }

    public Inventory getChestInventory() {
        return chestInventory;
    }

    public boolean isValid() {
        BlockState state = SnowarsPlugin.plugin.getServer().getWorld(blockWorld)
                .getBlockAt(blockX, blockY, blockZ)
                .getState();
        return state instanceof Chest;
    }

    public void onOpen() {
        validite();
        updateDisplay();
    }

    private void validite() {
        ItemStack[] storageContents = chestInventory.getStorageContents();
        ChestState tempState = ChestState.READY;
        for (int i = 0; i < storageContents.length; i++) {
            ItemStack storageContent = storageContents[i];
            if (storageContent != null && !storageContent.getType().equals(Material.AIR)) {
                if (state != ChestState.FINISHED) {
                    tempState = ChestState.ERROR;
                    errorMessage = "pool.error.not_empty";
                } else {
                    tempState = ChestState.FINISHED;
                }
                break;
            }
        }
        state = tempState;
    }

    public void removeDisplay() {
        messageDisplay.remove();
    }

    enum ChestState {
        READY, FINISHED, ERROR;
    }
}
