package net.oasis.createcraftsandcalls.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.oasis.createcraftsandcalls.registry.ModBlocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Вычисляет группу физически смежных блоков телефонной станции — по образцу
 * того, как соединяются сундуки Create в один большой сундук (см. ТЗ п.3.2).
 * <p>
 * Станция растёт только "вширь по своей плоскости": вверх, вниз и в стороны
 * вдоль своей лицевой стены — но не вглубь/наружу через стену (иначе
 * получилась бы толстая коробка вместо плоской панели с гнёздами).
 * <p>
 * Группа не кэшируется намеренно — станции обычно небольшие (единицы —
 * десятки блоков), а полный пересчёт BFS при каждом обращении гарантирует,
 * что группа всегда актуальна, даже если блок сломали в другом потоке кода.
 */
public final class StationGroup {

    private static final int MAX_GROUP_SIZE = 256;

    private StationGroup() {
    }

    /** Возвращает позиции всех блоков станции, физически соединённых с данным (включая его самого). */
    public static List<BlockPos> findGroup(Level level, BlockPos start) {
        BlockState startState = level.getBlockState(start);
        if (!startState.is(ModBlocks.STATION.get())) {
            return List.of();
        }
        Direction facing = startState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? startState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        List<Direction> growthDirections = lateralDirections(facing);

        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> result = new ArrayList<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && result.size() < MAX_GROUP_SIZE) {
            BlockPos current = queue.poll();
            result.add(current);
            for (Direction dir : growthDirections) {
                BlockPos neighborPos = current.relative(dir);
                if (visited.contains(neighborPos)) {
                    continue;
                }
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.is(ModBlocks.STATION.get())) {
                    visited.add(neighborPos);
                    queue.add(neighborPos);
                }
            }
        }
        return result;
    }

    /** Направления, вдоль которых станция может физически расти (не включает "вперёд/назад" сквозь панель). */
    private static List<Direction> lateralDirections(Direction facing) {
        Direction.Axis facingAxis = facing.getAxis();
        List<Direction> directions = new ArrayList<>();
        directions.add(Direction.UP);
        directions.add(Direction.DOWN);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (dir.getAxis() != facingAxis) {
                directions.add(dir);
            }
        }
        return directions;
    }

    /** Позиция "главного" блока группы — самая нижняя, затем самая северная, затем самая западная. Стабильный якорь для хранения маршрутов. */
    public static BlockPos findOrigin(Level level, BlockPos anyBlockInGroup) {
        List<BlockPos> group = findGroup(level, anyBlockInGroup);
        if (group.isEmpty()) {
            return anyBlockInGroup;
        }
        return group.stream()
                .min(Comparator.<BlockPos>comparingInt(BlockPos::getY)
                        .thenComparingInt(BlockPos::getZ)
                        .thenComparingInt(BlockPos::getX))
                .orElse(anyBlockInGroup);
    }

    /**
     * Глобальный индекс порта в пределах всей группы: индекс блока в
     * отсортированной группе * 4 (портов на блок) + локальный индекс порта.
     */
    public static int globalPortIndex(Level level, BlockPos blockPos, int localPortIndex) {
        List<BlockPos> group = sortedGroup(level, blockPos);
        int blockIndex = group.indexOf(blockPos);
        if (blockIndex < 0) {
            blockIndex = 0;
        }
        return blockIndex * StationBlockEntity.PORTS_PER_BLOCK + localPortIndex;
    }

    public static List<BlockPos> sortedGroup(Level level, BlockPos anyBlockInGroup) {
        List<BlockPos> group = new ArrayList<>(findGroup(level, anyBlockInGroup));
        group.sort(Comparator.<BlockPos>comparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getZ)
                .thenComparingInt(BlockPos::getX));
        return group;
    }
}