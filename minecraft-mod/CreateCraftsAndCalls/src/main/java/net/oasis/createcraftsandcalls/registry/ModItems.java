package net.oasis.createcraftsandcalls.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;
import net.oasis.createcraftsandcalls.item.PhoneCableItem;
import net.oasis.createcraftsandcalls.item.RouteWrenchItem;

/**
 * Регистрация всех предметов мода: предметы-блоки (телефон, станция),
 * телефонный кабель (для физического соединения телефона со станцией)
 * и вспомогательный предмет "маршрутный ключ" — на случай, если игрок
 * не использует ключ Create (обычный гаечный ключ Create тоже работает,
 * см. StationBlock#use).
 */
public class ModItems {

    public static final DeferredRegister.Items REGISTER =
            DeferredRegister.createItems(CreateCraftsAndCalls.MOD_ID);

    public static final DeferredItem<BlockItem> PHONE = REGISTER.registerItem("phone",
            props -> new BlockItem(ModBlocks.PHONE.get(), props),
            new Item.Properties());

    public static final DeferredItem<BlockItem> STATION = REGISTER.registerItem("station",
            props -> new BlockItem(ModBlocks.STATION.get(), props),
            new Item.Properties());

    public static final DeferredItem<PhoneCableItem> PHONE_CABLE = REGISTER.registerItem("phone_cable",
            PhoneCableItem::new,
            new Item.Properties());

    public static final DeferredItem<RouteWrenchItem> ROUTE_WRENCH = REGISTER.registerItem("route_wrench",
            RouteWrenchItem::new,
            new Item.Properties().stacksTo(1));
}
