package net.oasis.createcraftsandcalls.voicechat;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;
import net.oasis.createcraftsandcalls.station.StationBlockEntity;
import net.oasis.createcraftsandcalls.telephony.PortRef;

import java.util.UUID;

/**
 * Единая точка входа для интеграции со звонками через Simple Voice Chat.
 * <p>
 * Мод собирается и полностью работает без Simple Voice Chat — если мод не
 * установлен на сервере ({@link ModList#isLoaded}), все методы этого класса
 * просто ничего не делают (маршруты на станции по-прежнему коммутируются,
 * просто без реальной передачи голоса).
 */
public final class VoicechatBridge {

    private static volatile VoicechatServerApi serverApi;

    private VoicechatBridge() {
    }

    static void setServerApi(VoicechatServerApi api) {
        serverApi = api;
    }

    private static boolean isAvailable() {
        return ModList.get().isLoaded("voicechat") && serverApi != null;
    }

    /**
     * Игрок поднял трубку своего телефона. Если телефон подключён к станции
     * и на этом порту есть активный маршрут — пытаемся найти телефон на
     * другом конце провода и, если у него тоже поднята трубка, соединяем
     * голосовые каналы обоих игроков.
     */
    public static void onHandsetLifted(Level level, BlockPos phonePos, UUID player) {
        if (!isAvailable()) {
            return;
        }
        tryEstablishCall(level, phonePos, player);
    }

    public static void onHandsetPutDown(UUID player) {
        if (!isAvailable()) {
            return;
        }
        CallGroupManager.disconnect(serverApi, player);
    }

    /**
     * Вызывается телефонной станцией, когда телефонистка замыкает или
     * разрывает маршрут гаечным ключом — пробуем (пере)установить звонок
     * для обоих концов свежесозданного маршрута.
     */
    public static void onRouteConnected(Level level, PortRef inputPort, PortRef outputPort) {
        if (!isAvailable() || level.isClientSide) {
            return;
        }
        PhoneEndpoint a = PhoneEndpoint.find(level, inputPort);
        PhoneEndpoint b = PhoneEndpoint.find(level, outputPort);
        if (a == null || b == null) {
            return;
        }
        if (a.handsetLifted() && b.handsetLifted()) {
            CallGroupManager.connect(serverApi, a.playerId(), b.playerId());
        }
    }

    public static void onRouteDisconnected(UUID... players) {
        if (!isAvailable()) {
            return;
        }
        for (UUID player : players) {
            if (player != null) {
                CallGroupManager.disconnect(serverApi, player);
            }
        }
    }

    private static void tryEstablishCall(Level level, BlockPos phonePos, UUID player) {
        var phoneBe = level.getBlockEntity(phonePos);
        if (!(phoneBe instanceof net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity phone) || !phone.isLinked()) {
            return;
        }

        BlockPos stationPos = phone.getLinkedStationPos();
        var stationBe = level.getBlockEntity(stationPos);
        if (!(stationBe instanceof StationBlockEntity station)) {
            return;
        }

        PortRef myPort = new PortRef(station.getGroupOrigin(), phone.getLinkedPortIndex());
        PortRef otherPort = station.getRouting().getRouteOutput(myPort);
        if (otherPort == null) {
            // возможно, наш порт сам является выходом чьего-то маршрута — ищем обратную связь
            for (var entry : station.getRouting().getAllRoutes().entrySet()) {
                if (entry.getValue().equals(myPort)) {
                    otherPort = entry.getKey();
                    break;
                }
            }
        }
        if (otherPort == null) {
            return;
        }

        PhoneEndpoint other = PhoneEndpoint.find(level, otherPort);
        if (other != null && other.handsetLifted()) {
            CallGroupManager.connect(serverApi, player, other.playerId());
        }
    }

    /** Небольшой снимок телефона, подключённого к конкретному порту станции. */
    private record PhoneEndpoint(UUID playerId, boolean handsetLifted) {
        static PhoneEndpoint find(Level level, PortRef port) {
            StationBlockEntity station = StationBlockEntity.getStationServing(level, port.stationPos());
            if (station == null) {
                return null;
            }
            BlockPos phonePos = station.getPhoneBoundToPort(port.portIndex());
            if (phonePos == null) {
                return null;
            }
            if (!(level.getBlockEntity(phonePos) instanceof net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity phone)) {
                return null;
            }
            UUID bound = phone.getBoundPlayer();
            if (bound == null) {
                return null;
            }
            return new PhoneEndpoint(bound, phone.isHandsetLifted());
        }
    }
}
