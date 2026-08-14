package me.oasis.auctionshop.gui;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Небольшой помощник для добавления описания (lore) к предметам в GUI —
 * цена, продавец, категория и т.д.
 */
public final class ItemLore {

    private ItemLore() {
    }

    public static ItemStack withLore(ItemStack original, List<String> lore) {
        ItemStack copy = original.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta != null) {
            List<String> newLore = new ArrayList<>();
            List<String> existing = meta.getLore();
            if (existing != null) {
                newLore.addAll(existing);
                newLore.add("");
            }
            newLore.addAll(lore);
            meta.setLore(newLore);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    public static String money(double amount) {
        if (amount == Math.floor(amount)) {
            return String.format("%,.0f", amount);
        }
        return String.format("%,.2f", amount);
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
