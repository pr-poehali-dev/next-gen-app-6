package net.oasis.createcraftsandcalls.station;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.oasis.createcraftsandcalls.registry.ModBlockEntities;
import net.oasis.createcraftsandcalls.telephony.PortRef;
import org.jetbrains.annotations.Nullable;

/**
 * Телефонная станция (главный коммутатор). Реализует {@link IWrenchable} из
 * Create — гаечный ключ Create автоматически включает режим маршрутизации:
 * Shift+ПКМ ключом по гнезду выбирает вход/выход и (пере)соединяет маршрут,
 * согласно ТЗ п.4. Обычный клик рукой без ключа просто показывает состояние
 * гнёзд, не изменяя коммутацию.
 * <p>
 * Несколько смежных блоков станции физически объединяются в одну станцию —
 * см. {@link StationGroup}. Направление роста — вверх/вниз и в стороны вдоль
 * своей лицевой плоскости, как у сундука Create.
 */
public class StationBlock extends HorizontalDirectionalBlock implements EntityBlock, IWrenchable {

    public static final MapCodec<StationBlock> CODEC = simpleCodec(StationBlock::new);

    public StationBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * Клик рукой (без ключа) по станции — просто печатает игроку состояние
     * гнёзд этого блока (какие заняты проводом, какие свободны). Реальная
     * коммутация маршрутов делается только гаечным ключом (см. onWrenched).
     */
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof StationBlockEntity station)) {
            return InteractionResult.PASS;
        }
        StationBlockEntity origin = StationBlockEntity.getStationServing(level, pos);
        int activeRoutes = origin != null ? origin.getRouting().getActiveRouteCount() : 0;
        player.displayClientMessage(
                Component.literal("§bТелефонная станция §7— активных соединений: §e" + activeRoutes), true);
        return InteractionResult.CONSUME;
    }

    /**
     * Shift+ПКМ гаечным ключом Create — режим "телефонистки" (ТЗ п.4).
     * Гнездо, по которому кликнули, определяется по позиции удара мышью
     * внутри блока (левая/правая половина = вход/выход по X, верх/низ по Y).
     */
    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof StationBlockEntity clicked)) {
            return InteractionResult.PASS;
        }

        int localPort = resolveClickedPort(context, state);
        int globalPort = StationGroup.globalPortIndex(level, pos, localPort);
        BlockPos origin = StationGroup.findOrigin(level, pos);
        if (!(level.getBlockEntity(origin) instanceof StationBlockEntity station)) {
            return InteractionResult.PASS;
        }

        PortRef clickedRef = new PortRef(origin, globalPort);
        RoutingManager.RouteClickResult result = station.getRouting().handleWrenchClick(clickedRef);
        announceRouteResult(context.getPlayer(), result);

        if (result.type() == RoutingManager.RouteClickResult.Type.CONNECTED) {
            net.oasis.createcraftsandcalls.voicechat.VoicechatBridge.onRouteConnected(level, result.a(), result.b());
        } else if (result.type() == RoutingManager.RouteClickResult.Type.DISCONNECTED) {
            disconnectPortPlayers(level, result.a(), result.b());
        }

        station.setChanged();
        // Синхронизируем NBT станции (маршруты/метки портов) на клиент сразу после клика ключом.
        level.sendBlockUpdated(origin, level.getBlockState(origin), level.getBlockState(origin), 3);
        return InteractionResult.CONSUME;
    }

    /** Обычный ПКМ ключом (без Shift) по станции ничего не крутит — крутить тут нечего, гасим вращение блока. */
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    private int resolveClickedPort(UseOnContext context, BlockState state) {
        net.minecraft.world.phys.Vec3 hit = context.getClickLocation();
        BlockPos pos = context.getClickedPos();
        double localX = hit.x - pos.getX();
        double localY = hit.y - pos.getY();
        int column = localX < 0.5 ? 0 : 1;
        int row = localY < 0.5 ? 1 : 0;
        return row * 2 + column;
    }

    private void announceRouteResult(@Nullable Player player, RoutingManager.RouteClickResult result) {
        if (player == null) {
            return;
        }
        switch (result.type()) {
            case PENDING_SET -> player.displayClientMessage(
                    Component.literal("§eВход выбран. Кликните по гнезду для соединения."), true);
            case PENDING_CANCELLED -> player.displayClientMessage(
                    Component.literal("§7Выбор отменён."), true);
            case CONNECTED -> player.displayClientMessage(
                    Component.literal("§aСоединение установлено!"), true);
            case DISCONNECTED -> player.displayClientMessage(
                    Component.literal("§cСоединение разорвано."), true);
        }
    }

    private void disconnectPortPlayers(Level level, PortRef a, PortRef b) {
        java.util.UUID playerA = findBoundPlayer(level, a);
        java.util.UUID playerB = findBoundPlayer(level, b);
        net.oasis.createcraftsandcalls.voicechat.VoicechatBridge.onRouteDisconnected(playerA, playerB);
    }

    @Nullable
    private java.util.UUID findBoundPlayer(Level level, PortRef port) {
        StationBlockEntity station = StationBlockEntity.getStationServing(level, port.stationPos());
        if (station == null) {
            return null;
        }
        BlockPos phonePos = station.getPhoneBoundToPort(port.portIndex());
        if (phonePos != null && level.getBlockEntity(phonePos) instanceof net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity phone) {
            return phone.getBoundPlayer();
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof StationBlockEntity station) {
                station.tick();
            }
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof StationBlockEntity station) {
            // Разрываем все звонки, чей маршрут проходил через уничтожаемый блок.
            for (int local = 0; local < StationBlockEntity.PORTS_PER_BLOCK; local++) {
                station.removeNode(local, false);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}