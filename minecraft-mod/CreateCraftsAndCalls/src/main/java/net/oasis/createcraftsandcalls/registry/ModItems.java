package net.oasis.createcraftsandcalls.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;

/**
 * Регистрация предметов-блоков мода: телефонный аппарат и телефонная станция.
 * <p>
 * Отдельный предмет "медный провод" не регистрируется — для физического
 * соединения телефона со станцией используется штатная медная катушка
 * (Wire Spool) из мода Create: Crafts & Additions, как и требуется по ТЗ.
 * Оба наших блока реализуют интерфейс {@code IWireNode} из C&A, поэтому
 * катушка провода C&A уже "из коробки" умеет соединять их между собой.
 * <p>
 * Отдельный "маршрутный ключ" тоже не нужен — телефонная станция реализует
 * {@code IWrenchable} из Create, поэтому обычный гаечный ключ Create
 * (Shift+ПКМ) сразу запускает режим переключения маршрутов.
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
}
