package net.oasis.createcraftsandcalls.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Телефонный аппарат. ПКМ — поднять трубку (включить микрофон в SVC, если
 * маршрут через станцию установлен). Shift + ПКМ — положить трубку.
 * Первый игрок, который поднимает трубку впервые (после установки блока),
 * автоматически "привязывается" к этому телефону — именно его голос будет
 * передаваться по этому аппарату.
 * <p>
 * Свойство {@code handset_lifted} (переиспользуем ванильное {@code OPEN})
 * управляет тем, какую модель показывать — "трубка на месте" или "трубка
 * снята" (см. референс из ТЗ: два раздельных варианта модели телефона).
 * Пока используется общая модель-заглушка; когда появятся готовые 3D модели,
 * для каждого состояния можно задать свою модель в blockstates/phone.json.
 */
public class PhoneBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<PhoneBlock> CODEC = simpleCodec(PhoneBlock::new);
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty HANDSET_LIFTED = BlockStateProperties.OPEN;

    public PhoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HANDSET_LIFTED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, HANDSET_LIFTED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
                setHandsetState(level, pos, state, false);
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
            setHandsetState(level, pos, state, true);
            phone.setHandsetLifted(true);
            if (phone.isLinked()) {
                player.displayClientMessage(Component.literal("§aТрубка поднята. Ожидайте соединения телефонистки."), true);
            } else {
                player.displayClientMessage(Component.literal("§eТелефон не подключён к станции — свяжитесь с администратором."), true);
            }
        }

        return InteractionResult.CONSUME;
    }

    private void setHandsetState(Level level, BlockPos pos, BlockState state, boolean lifted) {
        level.setBlock(pos, state.setValue(HANDSET_LIFTED, lifted), 3);
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
