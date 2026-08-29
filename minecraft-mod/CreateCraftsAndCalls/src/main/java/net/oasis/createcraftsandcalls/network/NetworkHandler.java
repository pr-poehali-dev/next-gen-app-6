package net.oasis.createcraftsandcalls.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Точка регистрации сетевых пакетов мода.
 * <p>
 * На данный момент отдельные кастомные пакеты не нужны: состояние станции
 * (маршруты, привязанные телефоны) синхронизируется штатным механизмом
 * блок-сущностей Minecraft — {@code BlockEntity#getUpdatePacket()} /
 * {@code getUpdateTag()} (см. {@link net.oasis.createcraftsandcalls.station.StationBlockEntity}
 * и {@link net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity}).
 * Этот класс оставлен как единая точка расширения, если в будущем
 * понадобятся собственные пакеты (например, анимация трубки на клиенте).
 */
public final class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private NetworkHandler() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // Зарезервировано для будущих пакетов.
    }
}
