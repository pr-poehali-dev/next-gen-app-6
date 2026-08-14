package me.oasis.auctionshop.gui;

import me.oasis.auctionshop.auction.AuctionListing;
import me.oasis.auctionshop.auction.AuctionManager;
import me.oasis.auctionshop.shop.ShopItem;
import me.oasis.auctionshop.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Единая точка обработки всех кликов по инвентарям плагина —
 * определяет тип открытого окна по GuiHolder и делегирует нужную логику.
 */
public class GuiListener implements Listener {

    private final AuctionManager auctionManager;
    private final ShopManager shopManager;

    public GuiListener(AuctionManager auctionManager, ShopManager shopManager) {
        this.auctionManager = auctionManager;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder rawHolder = event.getInventory().getHolder();
        if (!(rawHolder instanceof GuiHolder holder)) {
            return;
        }

        // Запрещаем перетаскивать предметы GUI-инвентарей мышью в свой инвентарь
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        switch (holder.getType()) {
            case AUCTION_LIST -> handleAuctionListClick(player, event, holder);
            case MY_LISTINGS -> handleMyListingsClick(player, event, holder);
            case MAILBOX -> handleMailboxClick(player, event, holder);
            case SHOP_LIST -> handleShopClick(player, event, holder);
            case CONFIRM_SELL -> {
                // зарезервировано для будущих подтверждений
            }
        }
    }

    private void handleAuctionListClick(Player player, InventoryClickEvent event, GuiHolder holder) {
        int slot = event.getRawSlot();
        PlayerGuiState state = PlayerGuiState.get(player.getUniqueId());

        if (slot == AuctionGui.getSlotPrevPage()) {
            if (state.getPage() > 0) {
                state.setPage(state.getPage() - 1);
            }
            player.openInventory(AuctionGui.build(player, auctionManager));
            return;
        }
        if (slot == AuctionGui.getSlotNextPage()) {
            state.setPage(state.getPage() + 1);
            player.openInventory(AuctionGui.build(player, auctionManager));
            return;
        }
        if (slot == AuctionGui.getSlotCategory()) {
            state.setCategory(state.getCategory().next());
            state.setPage(0);
            player.openInventory(AuctionGui.build(player, auctionManager));
            return;
        }
        if (slot == AuctionGui.getSlotSort()) {
            state.setSortMode(state.getSortMode().next());
            state.setPage(0);
            player.openInventory(AuctionGui.build(player, auctionManager));
            return;
        }
        if (slot == AuctionGui.getSlotMyListings()) {
            player.openInventory(AuctionGui.buildMyListings(player, auctionManager));
            return;
        }
        if (slot == AuctionGui.getSlotMailbox()) {
            player.openInventory(AuctionGui.buildMailbox(player, auctionManager));
            return;
        }

        if (!AuctionGui.isItemSlot(slot)) {
            return;
        }

        Integer index = AuctionGui.slotToListingIndex(slot, holder.getPage());
        if (index == null) {
            return;
        }

        List<AuctionListing> listings = auctionManager.getSortedFiltered(state.getCategory(), state.getSortMode());
        if (index >= listings.size()) {
            return;
        }

        AuctionListing listing = listings.get(index);
        boolean success = auctionManager.buy(player, listing.getId());
        if (success) {
            player.sendMessage(ChatColor.GREEN + "Вы купили лот за " + ItemLore.money(listing.getPrice()) + " монет!");
        } else {
            player.sendMessage(ChatColor.RED + "Недостаточно монет для покупки, либо лот уже продан.");
        }
        player.openInventory(AuctionGui.build(player, auctionManager));
    }

    private void handleMyListingsClick(Player player, InventoryClickEvent event, GuiHolder holder) {
        int slot = event.getRawSlot();
        if (!AuctionGui.isItemSlot(slot)) {
            return;
        }

        Integer index = AuctionGui.slotToListingIndex(slot, 0);
        if (index == null) {
            return;
        }

        List<AuctionListing> mine = auctionManager.getListingsBySeller(player.getUniqueId());
        if (index >= mine.size()) {
            return;
        }

        AuctionListing listing = mine.get(index);
        boolean cancelled = auctionManager.cancelListing(player, listing.getId());
        if (cancelled) {
            player.sendMessage(ChatColor.YELLOW + "Лот снят с продажи, предмет возвращён.");
        }
        player.openInventory(AuctionGui.buildMyListings(player, auctionManager));
    }

    private void handleMailboxClick(Player player, InventoryClickEvent event, GuiHolder holder) {
        int slot = event.getRawSlot();
        if (!AuctionGui.isItemSlot(slot)) {
            return;
        }

        ItemStack clicked = event.getInventory().getItem(slot);
        if (clicked == null) {
            return;
        }

        List<ItemStack> box = auctionManager.getMailbox(player.getUniqueId());
        Integer index = AuctionGui.slotToListingIndex(slot, 0);
        if (index == null || index >= box.size()) {
            return;
        }

        ItemStack original = box.get(index);
        java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(original.clone());
        if (leftover.isEmpty()) {
            auctionManager.clearMailboxItem(player.getUniqueId(), original);
            player.sendMessage(ChatColor.GREEN + "Предмет получен!");
            player.openInventory(AuctionGui.buildMailbox(player, auctionManager));
        } else {
            player.sendMessage(ChatColor.RED + "Недостаточно места в инвентаре.");
        }
    }

    private void handleShopClick(Player player, InventoryClickEvent event, GuiHolder holder) {
        int slot = event.getRawSlot();
        PlayerGuiState state = PlayerGuiState.get(player.getUniqueId());

        if (slot == ShopGui.getSlotPrevPage()) {
            if (state.getPage() > 0) {
                state.setPage(state.getPage() - 1);
            }
            player.openInventory(ShopGui.build(player, shopManager));
            return;
        }
        if (slot == ShopGui.getSlotNextPage()) {
            state.setPage(state.getPage() + 1);
            player.openInventory(ShopGui.build(player, shopManager));
            return;
        }
        if (slot == ShopGui.getSlotCategory()) {
            state.setCategory(state.getCategory().next());
            state.setPage(0);
            player.openInventory(ShopGui.build(player, shopManager));
            return;
        }
        if (slot == ShopGui.getSlotSort()) {
            state.setSortMode(state.getSortMode().next());
            state.setPage(0);
            player.openInventory(ShopGui.build(player, shopManager));
            return;
        }

        if (!ShopGui.isItemSlot(slot)) {
            return;
        }

        Integer index = ShopGui.slotToItemIndex(slot, holder.getPage());
        if (index == null) {
            return;
        }

        List<ShopItem> shopItems = shopManager.getSortedFiltered(state.getCategory(), state.getSortMode());
        if (index >= shopItems.size()) {
            return;
        }

        ShopItem shopItem = shopItems.get(index);
        ClickType click = event.getClick();
        UUID itemId = shopItem.getId();

        boolean result;
        String action;
        if (click == ClickType.SHIFT_LEFT) {
            result = shopManager.sell(player, itemId, 1);
            action = "продажа 1 шт";
        } else if (click == ClickType.SHIFT_RIGHT) {
            result = shopManager.sell(player, itemId, 64);
            action = "продажа стака";
        } else if (click == ClickType.RIGHT) {
            result = shopManager.buy(player, itemId, 64);
            action = "покупка стака";
        } else {
            result = shopManager.buy(player, itemId, 1);
            action = "покупка 1 шт";
        }

        if (result) {
            player.sendMessage(ChatColor.GREEN + "Операция выполнена: " + action);
        } else {
            player.sendMessage(ChatColor.RED + "Не удалось выполнить операцию (недостаточно монет или предметов).");
        }

        player.openInventory(ShopGui.build(player, shopManager));
    }
}
