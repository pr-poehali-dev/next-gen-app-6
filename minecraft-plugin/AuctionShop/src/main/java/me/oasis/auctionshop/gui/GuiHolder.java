package me.oasis.auctionshop.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Общий маркер для всех инвентарей плагина — позволяет слушателю кликов
 * понять, что клик произошёл именно в нашем GUI, и достать нужные данные.
 */
public class GuiHolder implements InventoryHolder {

    public enum Type {
        AUCTION_LIST,
        MY_LISTINGS,
        MAILBOX,
        SHOP_LIST,
        CONFIRM_SELL
    }

    private final Type type;
    private final int page;
    private Inventory inventory;

    public GuiHolder(Type type, int page) {
        this.type = type;
        this.page = page;
    }

    public Type getType() {
        return type;
    }

    public int getPage() {
        return page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
