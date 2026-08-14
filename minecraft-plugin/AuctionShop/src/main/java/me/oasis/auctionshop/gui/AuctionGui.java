package me.oasis.auctionshop.gui;

import me.oasis.auctionshop.auction.AuctionListing;
import me.oasis.auctionshop.auction.AuctionManager;
import me.oasis.auctionshop.util.ItemCategory;
import me.oasis.auctionshop.util.SortMode;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Главное окно аукциона: список всех выставленных лотов с постраничным
 * пролистыванием, фильтром по категории и переключением сортировки.
 */
public class AuctionGui {

    public static final int SIZE = 54;
    private static final int[] ITEM_SLOTS = buildItemSlots();
    private static final int SLOT_PREV_PAGE = 45;
    private static final int SLOT_CATEGORY = 48;
    private static final int SLOT_SORT = 49;
    private static final int SLOT_MY_LISTINGS = 50;
    private static final int SLOT_MAILBOX = 51;
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

    public static Inventory build(Player player, AuctionManager auctionManager) {
        PlayerGuiState state = PlayerGuiState.get(player.getUniqueId());

        GuiHolder holder = new GuiHolder(GuiHolder.Type.AUCTION_LIST, state.getPage());
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, SIZE,
                ChatColor.DARK_PURPLE + "Аукцион");
        holder.setInventory(inv);

        List<AuctionListing> listings = auctionManager.getSortedFiltered(state.getCategory(), state.getSortMode());

        int perPage = ITEM_SLOTS.length;
        int totalPages = Math.max(1, (int) Math.ceil(listings.size() / (double) perPage));
        if (state.getPage() >= totalPages) {
            state.setPage(totalPages - 1);
        }
        int fromIndex = state.getPage() * perPage;
        int toIndex = Math.min(fromIndex + perPage, listings.size());

        if (fromIndex < listings.size()) {
            List<AuctionListing> pageItems = listings.subList(fromIndex, toIndex);
            for (int i = 0; i < pageItems.size(); i++) {
                AuctionListing listing = pageItems.get(i);
                ItemStack display = ItemLore.withLore(listing.getItem(), Arrays.asList(
                        ChatColor.GRAY + "Продавец: " + ChatColor.WHITE + listing.getSellerName(),
                        ChatColor.GRAY + "Цена: " + ChatColor.GOLD + ItemLore.money(listing.getPrice()) + " монет",
                        "",
                        ChatColor.YELLOW + "Кликните, чтобы купить"
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
        inv.setItem(SLOT_MY_LISTINGS, navItem(Material.PAPER, ChatColor.GREEN + "Мои лоты"));
        inv.setItem(SLOT_MAILBOX, navItem(Material.ENDER_CHEST, ChatColor.LIGHT_PURPLE + "Посылки (непереданные предметы)"));

        return inv;
    }

    public static Inventory buildMyListings(Player player, AuctionManager auctionManager) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.MY_LISTINGS, 0);
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, SIZE, ChatColor.DARK_PURPLE + "Мои лоты");
        holder.setInventory(inv);

        List<AuctionListing> mine = auctionManager.getListingsBySeller(player.getUniqueId());
        for (int i = 0; i < mine.size() && i < ITEM_SLOTS.length; i++) {
            AuctionListing listing = mine.get(i);
            ItemStack display = ItemLore.withLore(listing.getItem(), Arrays.asList(
                    ChatColor.GRAY + "Цена: " + ChatColor.GOLD + ItemLore.money(listing.getPrice()) + " монет",
                    "",
                    ChatColor.RED + "Кликните, чтобы снять с продажи"
            ));
            inv.setItem(ITEM_SLOTS[i], display);
        }
        return inv;
    }

    public static Inventory buildMailbox(Player player, AuctionManager auctionManager) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.MAILBOX, 0);
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, SIZE, ChatColor.DARK_PURPLE + "Посылки");
        holder.setInventory(inv);

        List<ItemStack> box = auctionManager.getMailbox(player.getUniqueId());
        for (int i = 0; i < box.size() && i < ITEM_SLOTS.length; i++) {
            ItemStack display = ItemLore.withLore(box.get(i), List.of(ChatColor.YELLOW + "Кликните, чтобы забрать"));
            inv.setItem(ITEM_SLOTS[i], display);
        }
        return inv;
    }

    public static Integer slotToListingIndex(int slot, int page) {
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

    public static int getSlotMyListings() {
        return SLOT_MY_LISTINGS;
    }

    public static int getSlotMailbox() {
        return SLOT_MAILBOX;
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
