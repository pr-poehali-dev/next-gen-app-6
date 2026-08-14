package me.oasis.auctionshop.util;

import org.bukkit.Material;

/**
 * Категории предметов. Определяются автоматически по типу материала —
 * это используется и для сортировки в аукционе, и для вкладок в магазине.
 */
public enum ItemCategory {
    ALL("Все"),
    WEAPONS("Оружие"),
    TOOLS("Инструменты"),
    ARMOR("Броня"),
    FOOD("Еда"),
    POTIONS("Зелья"),
    REDSTONE("Редстоун"),
    SPAWN_EGGS("Яйца призыва"),
    BLOCKS("Блоки"),
    MISC("Разное");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Определяет категорию предмета по его материалу.
     */
    public static ItemCategory categoryOf(Material material) {
        String name = material.name();

        if (name.endsWith("_SWORD") || name.equals("TRIDENT") || name.equals("BOW")
                || name.equals("CROSSBOW") || name.contains("TIPPED_ARROW")) {
            return WEAPONS;
        }

        if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS")
                || name.contains("BOOTS") || name.equals("SHIELD") || name.equals("ELYTRA")
                || name.equals("TURTLE_HELMET")) {
            return ARMOR;
        }

        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE") || name.equals("SHEARS") || name.equals("FISHING_ROD")
                || name.equals("FLINT_AND_STEEL") || name.equals("COMPASS") || name.equals("CLOCK")
                || name.equals("SPYGLASS")) {
            return TOOLS;
        }

        if (name.contains("POTION")) {
            return POTIONS;
        }

        if (name.endsWith("_SPAWN_EGG")) {
            return SPAWN_EGGS;
        }

        if (material.isEdible()) {
            return FOOD;
        }

        if (name.contains("REDSTONE") || name.contains("REPEATER") || name.contains("COMPARATOR")
                || name.contains("PISTON") || name.equals("DISPENSER") || name.equals("DROPPER")
                || name.equals("HOPPER") || name.equals("OBSERVER") || name.equals("TARGET")
                || name.contains("LEVER") || name.contains("BUTTON") || name.contains("PRESSURE_PLATE")
                || name.contains("RAIL") || name.contains("LAMP") || name.contains("DAYLIGHT")) {
            return REDSTONE;
        }

        if (material.isBlock()) {
            return BLOCKS;
        }

        return MISC;
    }

    /**
     * Следующая категория по кругу — используется для кнопки-фильтра в GUI.
     */
    public ItemCategory next() {
        ItemCategory[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
