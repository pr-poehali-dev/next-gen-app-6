package me.oasis.auctionshop.util;

/**
 * Режимы сортировки списка лотов аукциона / товаров магазина.
 */
public enum SortMode {
    PRICE_ASC("Цена ↑"),
    PRICE_DESC("Цена ↓"),
    NEWEST("Сначала новые");

    private final String displayName;

    SortMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SortMode next() {
        SortMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
