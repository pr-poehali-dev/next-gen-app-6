package me.oasis.auctionshop.shop;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Товар в постоянном серверном магазине (не путать с лотом аукциона —
 * этот товар не пропадает после покупки, его можно купить сколько угодно раз,
 * пока хватает денег).
 */
public class ShopItem implements ConfigurationSerializable {

    private final UUID id;
    private final ItemStack item;
    private final double buyPrice;
    private final double sellPrice;

    public ShopItem(UUID id, ItemStack item, double buyPrice, double sellPrice) {
        this.id = id;
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public UUID getId() {
        return id;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("item", item);
        map.put("buyPrice", buyPrice);
        map.put("sellPrice", sellPrice);
        return map;
    }

    public static ShopItem deserialize(Map<String, Object> map) {
        UUID id = UUID.fromString((String) map.get("id"));
        ItemStack item = (ItemStack) map.get("item");
        double buyPrice = ((Number) map.get("buyPrice")).doubleValue();
        double sellPrice = ((Number) map.get("sellPrice")).doubleValue();
        return new ShopItem(id, item, buyPrice, sellPrice);
    }
}
