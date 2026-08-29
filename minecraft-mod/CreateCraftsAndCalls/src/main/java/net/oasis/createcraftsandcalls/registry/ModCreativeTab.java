package net.oasis.createcraftsandcalls.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;

/**
 * Вкладка в творческом инвентаре с блоками мода: телефонный аппарат и
 * телефонная станция.
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateCraftsAndCalls.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = REGISTER.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createcraftsandcalls"))
                    .withTabsBefore(CreativeModeTabs.REDSTONE_BLOCKS)
                    .icon(() -> ModItems.STATION.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PHONE.get());
                        output.accept(ModItems.STATION.get());
                    })
                    .build());
}
