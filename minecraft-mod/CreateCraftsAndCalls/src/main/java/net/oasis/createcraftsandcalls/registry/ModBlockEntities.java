package net.oasis.createcraftsandcalls.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;
import net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity;
import net.oasis.createcraftsandcalls.station.StationBlockEntity;

/**
 * Регистрация типов блок-сущностей (данные телефона и станции).
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateCraftsAndCalls.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PhoneBlockEntity>> PHONE =
            REGISTER.register("phone", () -> BlockEntityType.Builder.of(
                    PhoneBlockEntity::new, ModBlocks.PHONE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StationBlockEntity>> STATION =
            REGISTER.register("station", () -> BlockEntityType.Builder.of(
                    StationBlockEntity::new, ModBlocks.STATION.get()
            ).build(null));
}