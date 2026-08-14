package me.oasis.auctionshop.gui;

import me.oasis.auctionshop.shop.ShopItem;
import me.oasis.auctionshop.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Окно постоянного серверного магазина. ЛКМ — купить 1 шт, ПКМ — купить 64 шт (или сколько влезет в стак).
 * Шифт+ЛКМ — продать 1 шт из инвентаря, Шифт+ПКМ — продать весь стак такого предмета.
 */
public class ShopGui {

    public static final int SIZE = 54;
    private static final int[] ITEM_SLOTS = buildItemSlots();
    private static final int SLOT_CATEGORY = 48;
    private static final int SLOT_SORT = 49;
    private static final int SLOT_PREV_PAGE = 45;
    private static final int SLOT_NEXT_PAGE = 53;

    private static int[] buildItemSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    public static Inventory build(Player player, ShopManager shopManager) {
        PlayerGuiState state = PlayerGuiState.get(player.getUniqueId());

        GuiHolder holder = new GuiHolder(GuiHolder.Type.SHOP_LIST, state.getPage());
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, SIZE, ChatColor.DARK_GREEN + "Магазин сервера");
        holder.setInventory(inv);

        List<ShopItem> shopItems = shopManager.getSortedFiltered(state.getCategory(), state.getSortMode());

        int perPage = ITEM_SLOTS.length;
        int totalPages = Math.max(1, (int) Math.ceil(shopItems.size() / (double) perPage));
        if (state.getPage() >= totalPages) {
            state.setPage(totalPages - 1);
        }
        int fromIndex = state.getPage() * perPage;
        int toIndex = Math.min(fromIndex + perPage, shopItems.size());

        if (fromIndex < shopItems.size()) {
            List<ShopItem> pageItems = shopItems.subList(fromIndex, toIndex);
            for (int i = 0; i < pageItems.size(); i++) {
                ShopItem shopItem = pageItems.get(i);
                ItemStack display = ItemLore.withLore(shopItem.getItem(), Arrays.asList(
                        ChatColor.GREEN + "Купить 1 шт: " + ItemLore.money(shopItem.getBuyPrice()) + " монет (ЛКМ)",
                        ChatColor.GREEN + "Купить стак: " + ItemLore.money(shopItem.getBuyPrice() * 64) + " монет (ПКМ)",
                        ChatColor.RED + "Продать 1 шт: " + ItemLore.money(shopItem.getSellPrice()) + " монет (Shift+ЛКМ)",
                        ChatColor.RED + "Продать стак (Shift+ПКМ)"
                ));
                inv.setItem(ITEM_SLOTS[i], display);
            }
        }

        inv.setItem(SLOT_PREV_PAGE, navItem(Material.ARROW, ChatColor.YELLOW + "« Предыдущая страница"));
        inv.setItem(SLOT_NEXT_PAGE, navItem(Material.ARROW, ChatColor.YELLOW + "Следующая страница »"));
        inv.setItem(SLOT_CATEGORY, navItem(Material.CHEST,
                ChatColor.AQUA + "Категория: " + state.getCategory().getDisplayName(),
                ChatColor.GRAY + "Кликните для переключения"));
        inv.setItem(SLOT_SORT, navItem(Material.HOPPER,
                ChatColor.AQUA + "Сортировка: " + state.getSortMode().getDisplayName(),
                ChatColor.GRAY + "Кликните для переключения"));

        return inv;
    }

    public static Integer slotToItemIndex(int slot, int page) {
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            if (ITEM_SLOTS[i] == slot) {
                return page * ITEM_SLOTS.length + i;
            }
        }
        return null;
    }

    public static boolean isItemSlot(int slot) {
        for (int s : ITEM_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    public static int getSlotPrevPage() {
        return SLOT_PREV_PAGE;
    }

    public static int getSlotNextPage() {
        return SLOT_NEXT_PAGE;
    }

    public static int getSlotCategory() {
        return SLOT_CATEGORY;
    }

    public static int getSlotSort() {
        return SLOT_SORT;
    }

    private static ItemStack navItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
