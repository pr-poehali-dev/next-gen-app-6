package me.oasis.auctionshop.shop;

import me.oasis.auctionshop.economy.EconomyManager;
import me.oasis.auctionshop.util.ItemCategory;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Управляет постоянными товарами серверного магазина: покупка/продажа за игровую валюту.
 * Владелец сервера настраивает список товаров командой /shop additem.
 */
public class ShopManager {

    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final File file;
    private FileConfiguration config;

    private final Map<UUID, ShopItem> items = new HashMap<>();

    public ShopManager(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.file = new File(plugin.getDataFolder(), "shop.yml");
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось создать shop.yml", e);
            }
            config = YamlConfiguration.loadConfiguration(file);
            seedDefaultItems();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);

        List<?> raw = config.getList("items");
        if (raw != null) {
            for (Object obj : raw) {
                if (obj instanceof Map<?, ?> map) {
                    ShopItem item = ShopItem.deserialize((Map<String, Object>) map);
                    items.put(item.getId(), item);
                }
            }
        }
    }

    /**
     * При самом первом запуске плагина наполняем магазин базовым набором
     * ходовых блоков и ресурсов, чтобы владельцу сервера было от чего оттолкнуться.
     */
    private void seedDefaultItems() {
        addItem(new ItemStack(Material.DIRT), 2, 1);
        addItem(new ItemStack(Material.COBBLESTONE), 3, 1);
        addItem(new ItemStack(Material.OAK_LOG), 5, 2);
        addItem(new ItemStack(Material.IRON_INGOT), 25, 12);
        addItem(new ItemStack(Material.GOLD_INGOT), 40, 20);
        addItem(new ItemStack(Material.DIAMOND), 250, 120);
        addItem(new ItemStack(Material.EMERALD), 200, 100);
        addItem(new ItemStack(Material.BREAD), 4, 1);
        addItem(new ItemStack(Material.APPLE), 5, 2);
        addItem(new ItemStack(Material.ARROW), 3, 1);
    }

    public void save() {
        config.set("items", new ArrayList<>(items.values()));
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить shop.yml", e);
        }
    }

    public ShopItem addItem(ItemStack item, double buyPrice, double sellPrice) {
        ShopItem shopItem = new ShopItem(UUID.randomUUID(), item.clone(), buyPrice, sellPrice);
        items.put(shopItem.getId(), shopItem);
        save();
        return shopItem;
    }

    public boolean removeItem(UUID id) {
        boolean removed = items.remove(id) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public ShopItem getItem(UUID id) {
        return items.get(id);
    }

    public List<ShopItem> getAllItems() {
        return new ArrayList<>(items.values());
    }

    public List<ShopItem> getSortedFiltered(ItemCategory category, me.oasis.auctionshop.util.SortMode sortMode) {
        List<ShopItem> result = items.values().stream()
                .filter(i -> category == ItemCategory.ALL || ItemCategory.categoryOf(i.getItem().getType()) == category)
                .collect(Collectors.toList());

        switch (sortMode) {
            case PRICE_ASC -> result.sort(Comparator.comparingDouble(ShopItem::getBuyPrice));
            case PRICE_DESC -> result.sort(Comparator.comparingDouble(ShopItem::getBuyPrice).reversed());
            case NEWEST -> {
                // Для постоянного магазина «новизна» не применима — оставляем как есть
            }
        }
        return result;
    }

    /**
     * Покупка одной единицы товара (стак настраивается в GUI кликом ПКМ/ЛКМ на большее количество).
     */
    public boolean buy(Player player, UUID itemId, int amount) {
        ShopItem shopItem = items.get(itemId);
        if (shopItem == null) {
            return false;
        }
        double totalPrice = shopItem.getBuyPrice() * amount;
        if (!economy.withdraw(player.getUniqueId(), totalPrice)) {
            return false;
        }

        ItemStack toGive = shopItem.getItem().clone();
        toGive.setAmount(amount);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(toGive);
        leftover.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        return true;
    }

    /**
     * Продажа товара обратно в магазин из инвентаря игрока.
     */
    public boolean sell(Player player, UUID itemId, int amount) {
        ShopItem shopItem = items.get(itemId);
        if (shopItem == null) {
            return false;
        }

        ItemStack template = shopItem.getItem();
        int available = countMatching(player, template);
        if (available < amount) {
            return false;
        }

        removeMatching(player, template, amount);
        economy.deposit(player.getUniqueId(), shopItem.getSellPrice() * amount);
        return true;
    }

    private int countMatching(Player player, ItemStack template) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.isSimilar(template)) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removeMatching(Player player, ItemStack template, int amount) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        int remaining = amount;
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.isSimilar(template)) {
                int take = Math.min(remaining, stack.getAmount());
                stack.setAmount(stack.getAmount() - take);
                remaining -= take;
                if (stack.getAmount() <= 0) {
                    contents[i] = null;
                } else {
                    contents[i] = stack;
                }
            }
        }
        player.getInventory().setStorageContents(contents);
    }
}
