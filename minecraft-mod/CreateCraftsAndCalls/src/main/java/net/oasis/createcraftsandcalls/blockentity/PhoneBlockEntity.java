package net.oasis.createcraftsandcalls.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oasis.createcraftsandcalls.registry.ModBlockEntities;
import net.oasis.createcraftsandcalls.voicechat.VoicechatBridge;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Данные телефонного аппарата: снята ли трубка, к какому порту станции
 * подключён этот телефон (по координатам блока станции + номеру гнезда),
 * и владелец телефона (UUID игрока, который в последний раз был привязан к
 * этому аппарату — заполняется при первом подключении кабеля).
 */
public class PhoneBlockEntity extends BlockEntity {

    private boolean handsetLifted = false;
    private UUID boundPlayer;

    /** Позиция станции, к которой физически подключён этот телефон медным/телефонным кабелем. */
    private BlockPos linkedStationPos;
    /** Номер порта на станции (0..3 на блок станции, суммарно по всей мультиблочной станции). */
    private int linkedPortIndex = -1;

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
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
