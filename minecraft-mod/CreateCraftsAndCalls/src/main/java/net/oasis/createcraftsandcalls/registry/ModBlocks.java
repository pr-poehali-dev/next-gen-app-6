package net.oasis.createcraftsandcalls.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;
import net.oasis.createcraftsandcalls.block.PhoneBlock;
import net.oasis.createcraftsandcalls.station.StationBlock;

/**
 * Регистрация всех блоков мода: телефонный аппарат и телефонная станция.
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks REGISTER =
            DeferredRegister.createBlocks(CreateCraftsAndCalls.MOD_ID);

    public static final DeferredBlock<PhoneBlock> PHONE = REGISTER.register("phone",
            () -> new PhoneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()));

    public static final DeferredBlock<StationBlock> STATION = REGISTER.register("station",
            () -> new StationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(4.0f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()));
}