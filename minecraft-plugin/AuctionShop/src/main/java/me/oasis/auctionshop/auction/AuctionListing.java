package me.oasis.auctionshop.auction;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Один лот, выставленный игроком на аукцион.
 */
public class AuctionListing implements ConfigurationSerializable {

    private final UUID id;
    private final UUID sellerId;
    private final String sellerName;
    private final ItemStack item;
    private final double price;
    private final long createdAt;

    public AuctionListing(UUID id, UUID sellerId, String sellerName, ItemStack item, double price, long createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("sellerId", sellerId.toString());
        map.put("sellerName", sellerName);
        map.put("item", item);
        map.put("price", price);
        map.put("createdAt", createdAt);
        return map;
    }

    public static AuctionListing deserialize(Map<String, Object> map) {
        UUID id = UUID.fromString((String) map.get("id"));
        UUID sellerId = UUID.fromString((String) map.get("sellerId"));
        String sellerName = (String) map.get("sellerName");
        ItemStack item = (ItemStack) map.get("item");
        double price = ((Number) map.get("price")).doubleValue();
        long createdAt = ((Number) map.get("createdAt")).longValue();
        return new AuctionListing(id, sellerId, sellerName, item, price, createdAt);
    }
}
