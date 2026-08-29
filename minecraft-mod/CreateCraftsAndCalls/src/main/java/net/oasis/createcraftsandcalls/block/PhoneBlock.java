package net.oasis.createcraftsandcalls.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity;
import net.oasis.createcraftsandcalls.registry.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * Телефонный аппарат. ПКМ — поднять трубку (включить микрофон в SVC, если
 * маршрут через станцию установлен). Shift + ПКМ — положить трубку.
 * Первый игрок, который поднимает трубку впервые (после установки блока),
 * автоматически "привязывается" к этому телефону — именно его голос будет
 * передаваться по этому аппарату.
 */
public class PhoneBlock extends Block implements EntityBlock {

    public PhoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PhoneBlockEntity phone)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (phone.isHandsetLifted()) {
                phone.setHandsetLifted(false);
                player.displayClientMessage(Component.literal("§7Трубка положена."), true);
            }
            return InteractionResult.CONSUME;
        }

        if (phone.getBoundPlayer() == null) {
            phone.setBoundPlayer(player.getUUID());
        }

        if (!phone.getBoundPlayer().equals(player.getUUID())) {
            player.displayClientMessage(Component.literal("§cЭтот телефон уже закреплён за другим игроком."), true);
            return InteractionResult.CONSUME;
        }

        if (!phone.isHandsetLifted()) {
            phone.setHandsetLifted(true);
            if (phone.isLinked()) {
                player.displayClientMessage(Component.literal("§aТрубка поднята. Ожидайте соединения телефонистки."), true);
            } else {
                player.displayClientMessage(Component.literal("§eТелефон не подключён к станции — свяжитесь с администратором."), true);
            }
        }

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PhoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PhoneBlockEntity phone && phone.getBoundPlayer() != null) {
                net.oasis.createcraftsandcalls.voicechat.VoicechatBridge.onHandsetPutDown(phone.getBoundPlayer());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
