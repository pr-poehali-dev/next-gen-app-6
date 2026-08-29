package net.oasis.createcraftsandcalls.blockentity;

import com.mrh0.createaddition.blocks.connector.ConnectorType;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.LocalNode;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.energy.network.EnergyNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.oasis.createcraftsandcalls.registry.ModBlockEntities;
import net.oasis.createcraftsandcalls.station.StationBlockEntity;
import net.oasis.createcraftsandcalls.station.StationGroup;
import net.oasis.createcraftsandcalls.voicechat.VoicechatBridge;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Данные телефонного аппарата: снята ли трубка, к какому порту станции
 * подключён этот телефон, и владелец телефона (UUID игрока, привязанного к
 * этому аппарату при первом поднятии трубки).
 * <p>
 * Реализует {@link IWireNode} с одним-единственным гнездом (индекс 0) —
 * именно поэтому медную катушку провода Create: Crafts & Additions можно
 * дотянуть от станции прямо до телефона (ТЗ п.3.1: "К нему подводится
 * медный провод от Коммутатора").
 */
public class PhoneBlockEntity extends BlockEntity implements IWireNode {

    private static final int MAX_WIRE_LENGTH = 12;

    private boolean handsetLifted = false;
    private UUID boundPlayer;

    /** Позиция "главного" блока станции, к которой физически подключён этот телефон. */
    private BlockPos linkedStationPos;
    /** Глобальный номер порта на станции (по всей мультиблочной группе). */
    private int linkedPortIndex = -1;

    private final LocalNode[] localNodes = new LocalNode[1];
    private final IWireNode[] nodeCache = new IWireNode[1];
    private EnergyNetwork network;

    public PhoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PHONE.get(), pos, state);
    }

    public boolean isHandsetLifted() {
        return handsetLifted;
    }

    /**
     * Поднимает или кладёт трубку. При поднятии — просим VoicechatBridge включить
     * микрофон/связь для владельца телефона (если есть активный маршрут).
     * При положении трубки — разрываем передачу голоса.
     */
    public void setHandsetLifted(boolean lifted) {
        if (this.handsetLifted == lifted) {
            return;
        }
        this.handsetLifted = lifted;
        setChanged();
        syncToClient();

        if (level != null && !level.isClientSide && boundPlayer != null) {
            if (lifted) {
                VoicechatBridge.onHandsetLifted(level, worldPosition, boundPlayer);
            } else {
                VoicechatBridge.onHandsetPutDown(boundPlayer);
            }
        }
    }

    public UUID getBoundPlayer() {
        return boundPlayer;
    }

    public void setBoundPlayer(UUID boundPlayer) {
        this.boundPlayer = boundPlayer;
        setChanged();
    }

    public BlockPos getLinkedStationPos() {
        return linkedStationPos;
    }

    public int getLinkedPortIndex() {
        return linkedPortIndex;
    }

    public void setLink(BlockPos stationPos, int portIndex) {
        this.linkedStationPos = stationPos;
        this.linkedPortIndex = portIndex;
        setChanged();
        syncToClient();
    }

    public void clearLink() {
        this.linkedStationPos = null;
        this.linkedPortIndex = -1;
        setChanged();
        syncToClient();
    }

    public boolean isLinked() {
        return linkedStationPos != null && linkedPortIndex >= 0;
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ----- IWireNode: провод от станции подключается сюда как в обычный разъём -----

    @Override
    public IWireNode getWireNode(int index) {
        return IWireNode.getWireNodeFrom(index, this, localNodes, nodeCache, level);
    }

    @Override
    public LocalNode getLocalNode(int index) {
        return localNodes[0];
    }

    @Override
    public void setNode(int index, int otherIndex, BlockPos otherPos, WireType type) {
        localNodes[0] = new LocalNode(this, 0, otherIndex, type, otherPos);
        setChanged();
        if (network != null) {
            network.invalidate();
        }

        if (level != null && level.getBlockEntity(otherPos) instanceof StationBlockEntity station) {
            BlockPos origin = StationGroup.findOrigin(level, otherPos);
            int globalIndex = StationGroup.globalPortIndex(level, otherPos, otherIndex);
            station.registerPhone(otherIndex, worldPosition);
            setLink(origin, globalIndex);
        }
    }

    @Override
    public void removeNode(int index, boolean dropWire) {
        localNodes[0] = null;
        nodeCache[0] = null;
        invalidateNodeCache();
        setChanged();
        if (network != null) {
            network.invalidate();
        }
        clearLink();
    }

    @Override
    public Vec3 getNodeOffset(int index) {
        return new Vec3(0.5, 0.9, 0.5);
    }

    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public void invalidateNodeCache() {
        nodeCache[0] = null;
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("HandsetLifted", handsetLifted);
        if (boundPlayer != null) {
            tag.putUUID("BoundPlayer", boundPlayer);
        }
        if (linkedStationPos != null) {
            tag.putLong("LinkedStationPos", linkedStationPos.asLong());
            tag.putInt("LinkedPortIndex", linkedPortIndex);
        }
        if (localNodes[0] != null) {
            CompoundTag nodeTag = new CompoundTag();
            localNodes[0].write(nodeTag);
            tag.put("Node", nodeTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handsetLifted = tag.getBoolean("HandsetLifted");
        boundPlayer = tag.hasUUID("BoundPlayer") ? tag.getUUID("BoundPlayer") : null;
        if (tag.contains("LinkedStationPos")) {
            linkedStationPos = BlockPos.of(tag.getLong("LinkedStationPos"));
            linkedPortIndex = tag.getInt("LinkedPortIndex");
        } else {
            linkedStationPos = null;
            linkedPortIndex = -1;
        }
        if (tag.contains("Node")) {
            localNodes[0] = new LocalNode(this, tag.getCompound("Node"));
        } else {
            localNodes[0] = null;
        }
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
