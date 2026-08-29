package net.oasis.createcraftsandcalls;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.oasis.createcraftsandcalls.network.NetworkHandler;
import net.oasis.createcraftsandcalls.registry.ModBlockEntities;
import net.oasis.createcraftsandcalls.registry.ModBlocks;
import net.oasis.createcraftsandcalls.registry.ModCreativeTab;
import net.oasis.createcraftsandcalls.registry.ModItems;
import net.oasis.createcraftsandcalls.station.StationBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главная точка входа мода Create Crafts & Calls.
 * <p>
 * Мод добавляет телефонную систему: телефонный аппарат, телефонную трубку,
 * модульную телефонную станцию (растёт как сундук Create) и подключается
 * медным проводом из Create: Crafts & Additions. Станция коммутируется
 * гаечным ключом Create — игрок вручную соединяет вход и выход, как
 * настоящая телефонистка. Голос передаётся через мод Simple Voice Chat
 * (интеграция необязательная — если SVC не установлен, звонки просто не
 * будут передавать звук, но блоки и коммутация всё равно работают).
 */
@Mod(CreateCraftsAndCalls.MOD_ID)
public class CreateCraftsAndCalls {

    public static final String MOD_ID = "createcraftsandcalls";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public CreateCraftsAndCalls(IEventBus modEventBus) {
        ModBlocks.REGISTER.register(modEventBus);
        ModItems.REGISTER.register(modEventBus);
        ModBlockEntities.REGISTER.register(modEventBus);
        ModCreativeTab.REGISTER.register(modEventBus);

        modEventBus.addListener(NetworkHandler::register);
        modEventBus.addListener(StationBlockEntity::registerCapabilities);

        LOGGER.info("Create Crafts & Calls загружен. Телефонная станция готова к коммутации.");
    }
}