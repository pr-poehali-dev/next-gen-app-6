package me.oasis.auctionshop.gui;

import me.oasis.auctionshop.util.ItemCategory;
import me.oasis.auctionshop.util.SortMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хранит текущий выбранный фильтр по категории, режим сортировки и страницу
 * для каждого игрока, пока открыто GUI аукциона или магазина.
 */
public class PlayerGuiState {

    private static final Map<UUID, PlayerGuiState> STATES = new HashMap<>();

    private ItemCategory category = ItemCategory.ALL;
    private SortMode sortMode = SortMode.NEWEST;
    private int page = 0;

    public static PlayerGuiState get(UUID playerId) {
        return STATES.computeIfAbsent(playerId, k -> new PlayerGuiState());
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(SortMode sortMode) {
        this.sortMode = sortMode;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
