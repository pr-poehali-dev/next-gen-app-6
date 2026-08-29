package net.oasis.createcraftsandcalls.station;

import com.mrh0.createaddition.blocks.connector.ConnectorType;
import com.mrh0.createaddition.energy.IEnergyProvider;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.LocalNode;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.energy.network.EnergyNetwork;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.oasis.createcraftsandcalls.registry.ModBlockEntities;
import net.oasis.createcraftsandcalls.telephony.PortRef;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Блок-сущность телефонной станции. Каждый физический блок станции — это
 * маленький узел проводной сети Create Addition ({@link IWireNode}) с 4
 * гнёздами (портами), как описано в ТЗ п.3.2-3.3. Несколько смежных блоков
 * образуют одну логическую станцию — маршруты (см. {@link RoutingManager})
 * хранятся на "главном" блоке группы ({@link StationGroup#findOrigin}),
 * остальные блоки группы просто делегируют запросы к нему.
 * <p>
 * Энергопотребление (ТЗ п.6): 5000 FE за каждый блок станции + 1000 FE за
 * каждый подключённый телефон, который активно участвует в разговоре.
 */
public class StationBlockEntity extends SmartBlockEntity implements IWireNode, IEnergyProvider {

    public static final int PORTS_PER_BLOCK = 4;
    private static final int ENERGY_PER_BLOCK_TICK = 5000;
    private static final int ENERGY_PER_PHONE_TICK = 1000;
    private static final int MAX_WIRE_LENGTH = 8;

    private final LocalNode[] localNodes = new LocalNode[PORTS_PER_BLOCK];
    private final IWireNode[] nodeCache = new IWireNode[PORTS_PER_BLOCK];
    private EnergyNetwork network;

    /** Только заполнено на "главном" блоке группы — вся коммутация станции. */
    private final RoutingManager routing = new RoutingManager();
    /** localPortIndex (0..3 на этот конкретный блок) -> позиция телефона, физически подключённого проводом. */
    private final Map<Integer, BlockPos> phonesByLocalPort = new HashMap<>();

    private final com.mrh0.createaddition.energy.InternalEnergyStorage energy =
            new com.mrh0.createaddition.energy.InternalEnergyStorage(50_000, 5_000, 0);

    protected BlockCapabilityCache<IEnergyStorage, Direction> externalEnergy;
    private boolean firstTick = true;

    public StationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STATION.get(), pos, state);
    }

    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.STATION.get(),
                (be, side) -> be.energy);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Собственных поведений Create не требуется — вся логика реализована напрямую.
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        if (firstTick) {
            firstTick = false;
        }
        chargeAndConsumeEnergy();
    }

    /**
     * Каждый тик станция сначала пытается набрать энергию из внешнего кабеля
     * Create (redstone flux), затем расходует её на своё содержание и на
     * каждый активный маршрут с поднятой трубкой. Если энергии не хватает —
     * маршруты сбрасываются (ТЗ п.6: "при отключении электричества все
     * маршруты сбрасываются, разговоры обрываются").
     */
    private void chargeAndConsumeEnergy() {
        pullFromNeighbor();

        boolean isOrigin = StationGroup.findOrigin(level, worldPosition).equals(worldPosition);
        int required = ENERGY_PER_BLOCK_TICK;
        if (isOrigin) {
            required += routing.getActiveRouteCount() * ENERGY_PER_PHONE_TICK;
        }

        int extracted = energy.extractEnergy(required, false);
        if (extracted < required && isOrigin && routing.getActiveRouteCount() > 0) {
            dropAllRoutesDueToPowerLoss();
        }
    }

    private void pullFromNeighbor() {
        for (Direction direction : Direction.values()) {
            if (externalEnergy == null && level instanceof ServerLevel serverLevel) {
                externalEnergy = BlockCapabilityCache.create(
                        Capabilities.EnergyStorage.BLOCK, serverLevel, worldPosition.relative(direction), direction.getOpposite());
            }
        }
        if (level instanceof ServerLevel serverLevel) {
            for (Direction direction : Direction.values()) {
                IEnergyStorage neighbor = serverLevel.getCapability(Capabilities.EnergyStorage.BLOCK,
                        worldPosition.relative(direction), direction.getOpposite());
                if (neighbor != null && neighbor.canExtract()) {
                    int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
                    if (space <= 0) {
                        break;
                    }
                    int pulled = neighbor.extractEnergy(space, false);
                    if (pulled > 0) {
                        energy.receiveEnergy(pulled, false);
                    }
                }
            }
        }
    }

    private void dropAllRoutesDueToPowerLoss() {
        java.util.List<java.util.UUID> affected = new java.util.ArrayList<>();
        for (Map.Entry<PortRef, PortRef> route : Map.copyOf(routing.getAllRoutes()).entrySet()) {
            collectPhonePlayer(route.getKey(), affected);
            collectPhonePlayer(route.getValue(), affected);
        }
        routing.getAllRoutes().clear();
        routing.clearPending();
        if (level != null) {
            net.oasis.createcraftsandcalls.voicechat.VoicechatBridge.onRouteDisconnected(
                    affected.toArray(new java.util.UUID[0]));
        }
    }

    private void collectPhonePlayer(PortRef port, java.util.List<java.util.UUID> out) {
        StationBlockEntity station = getStationServing(level, port.stationPos());
        if (station == null) {
            return;
        }
        BlockPos phonePos = station.getPhoneBoundToPort(port.portIndex());
        if (phonePos != null && level.getBlockEntity(phonePos) instanceof net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity phone
                && phone.getBoundPlayer() != null) {
            out.add(phone.getBoundPlayer());
        }
    }

    // ----- Мультиблочная адресация -----

    public BlockPos getGroupOrigin() {
        return level != null ? StationGroup.findOrigin(level, worldPosition) : worldPosition;
    }

    @Nullable
    public static StationBlockEntity getStationServing(net.minecraft.world.level.Level level, BlockPos anyPosInGroup) {
        if (level == null) {
            return null;
        }
        BlockPos origin = StationGroup.findOrigin(level, anyPosInGroup);
        if (level.getBlockEntity(origin) instanceof StationBlockEntity be) {
            return be;
        }
        return null;
    }

    /** Возвращает менеджер маршрутов — валиден только на "главном" блоке группы. */
    public RoutingManager getRouting() {
        return routing;
    }

    public void registerPhone(int localPortIndex, BlockPos phonePos) {
        phonesByLocalPort.put(localPortIndex, phonePos);
        setChanged();
    }

    public void unregisterPhone(int localPortIndex) {
        phonesByLocalPort.remove(localPortIndex);
        setChanged();
    }

    @Nullable
    public BlockPos getPhoneBoundToPort(int globalPortIndex) {
        if (level == null) {
            return null;
        }
        List<BlockPos> group = StationGroup.sortedGroup(level, worldPosition);
        int blockIndex = globalPortIndex / PORTS_PER_BLOCK;
        int localIndex = globalPortIndex % PORTS_PER_BLOCK;
        if (blockIndex < 0 || blockIndex >= group.size()) {
            return null;
        }
        BlockPos blockPos = group.get(blockIndex);
        if (level.getBlockEntity(blockPos) instanceof StationBlockEntity be) {
            return be.phonesByLocalPort.get(localIndex);
        }
        return null;
    }

    // ----- IWireNode: провода Create Addition -----

    @Override
    public IWireNode getWireNode(int index) {
        return IWireNode.getWireNodeFrom(index, this, localNodes, nodeCache, level);
    }

    @Override
    public LocalNode getLocalNode(int index) {
        return localNodes[index];
    }

    @Override
    public void setNode(int index, int otherIndex, BlockPos otherPos, WireType type) {
        localNodes[index] = new LocalNode(this, index, otherIndex, type, otherPos);
        setChanged();
        if (network != null) {
            network.invalidate();
        }

        // Если провод подключён к телефону — запоминаем связь для маршрутизации звонков.
        if (level != null && level.getBlockEntity(otherPos) instanceof net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity phone) {
            registerPhone(index, otherPos);
            BlockPos origin = getGroupOrigin();
            int globalIndex = StationGroup.globalPortIndex(level, worldPosition, index);
            phone.setLink(origin, globalIndex);
        }
    }

    @Override
    public void removeNode(int index, boolean dropWire) {
        LocalNode removed = localNodes[index];
        localNodes[index] = null;
        nodeCache[index] = null;
        invalidateNodeCache();
        setChanged();
        if (network != null) {
            network.invalidate();
        }

        if (removed != null && level != null) {
            BlockPos otherPos = removed.getPos();
            if (level.getBlockEntity(otherPos) instanceof net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity phone) {
                phone.clearLink();
            }
            PortRef myPort = new PortRef(getGroupOrigin(), StationGroup.globalPortIndex(level, worldPosition, index));
            routing.removeRoutesInvolving(myPort);
        }
        unregisterPhone(index);
    }

    @Override
    public Vec3 getNodeOffset(int index) {
        // Гнёзда визуально расположены на лицевой стороне блока в 2 ряда по 2.
        double xOffset = (index % 2 == 0) ? 0.3 : 0.7;
        double yOffset = (index < 2) ? 0.7 : 0.3;
        return new Vec3(xOffset, yOffset, 0.5);
    }

    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public void invalidateNodeCache() {
        for (int i = 0; i < nodeCache.length; i++) {
            nodeCache[i] = null;
        }
    }

    @Override
    public void setNetwork(int index, EnergyNetwork network) {
        this.network = network;
    }

    @Override
    public EnergyNetwork getNetwork(int index) {
        return network;
    }

    @Override
    public ConnectorType getConnectorType() {
        return ConnectorType.Small;
    }

    @Override
    public int getMaxWireLength() {
        return MAX_WIRE_LENGTH;
    }

    @Override
    public IEnergyStorage getEnergyStorage(Direction direction) {
        return energy;
    }

    // ----- NBT -----

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Energy", energy.write(new CompoundTag()));

        ListTag nodesTag = new ListTag();
        for (LocalNode node : localNodes) {
            CompoundTag nodeTag = new CompoundTag();
            if (node != null) {
                node.write(nodeTag);
            }
            nodesTag.add(nodeTag);
        }
        tag.put("Nodes", nodesTag);

        ListTag phonesTag = new ListTag();
        for (Map.Entry<Integer, BlockPos> entry : phonesByLocalPort.entrySet()) {
            CompoundTag phoneTag = new CompoundTag();
            phoneTag.putInt("Port", entry.getKey());
            phoneTag.putLong("Pos", entry.getValue().asLong());
            phonesTag.add(phoneTag);
        }
        tag.put("Phones", phonesTag);

        if (StationGroup.findOrigin(level, worldPosition).equals(worldPosition)) {
            tag.put("Routing", writeRouting());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("Energy")) {
            energy.read(tag.getCompound("Energy"));
        }

        ListTag nodesTag = tag.getList("Nodes", 10);
        for (int i = 0; i < nodesTag.size() && i < localNodes.length; i++) {
            CompoundTag nodeTag = nodesTag.getCompound(i);
            localNodes[i] = nodeTag.isEmpty() ? null : new LocalNode(this, nodeTag);
        }

        phonesByLocalPort.clear();
        ListTag phonesTag = tag.getList("Phones", 10);
        for (int i = 0; i < phonesTag.size(); i++) {
            CompoundTag phoneTag = phonesTag.getCompound(i);
            phonesByLocalPort.put(phoneTag.getInt("Port"), BlockPos.of(phoneTag.getLong("Pos")));
        }

        if (tag.contains("Routing")) {
            readRouting(tag.getCompound("Routing"));
        }
    }

    private CompoundTag writeRouting() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<PortRef, PortRef> route : routing.getAllRoutes().entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("InStation", route.getKey().stationPos().asLong());
            entry.putInt("InPort", route.getKey().portIndex());
            entry.putLong("OutStation", route.getValue().stationPos().asLong());
            entry.putInt("OutPort", route.getValue().portIndex());
            list.add(entry);
        }
        tag.put("Routes", list);
        return tag;
    }

    private void readRouting(CompoundTag tag) {
        ListTag list = tag.getList("Routes", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            PortRef in = new PortRef(BlockPos.of(entry.getLong("InStation")), entry.getInt("InPort"));
            PortRef out = new PortRef(BlockPos.of(entry.getLong("OutStation")), entry.getInt("OutPort"));
            routing.getAllRoutes().put(in, out);
        }
    }

    // getUpdatePacket()/getUpdateTag() уже реализованы в SyncedBlockEntity (родитель
    // SmartBlockEntity из Create) и синхронизируют весь write()/read() автоматически.
}