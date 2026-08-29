package net.oasis.createcraftsandcalls.station;

import net.minecraft.core.BlockPos;
import net.oasis.createcraftsandcalls.telephony.PortRef;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Таблица активных маршрутов телефонной станции (одной физической станции,
 * идентифицируемой позицией её "главного" блока — самого нижнего и самого
 * западного/северного блока в мультиблочной группе).
 * <p>
 * Маршрут — это связь "порт-вход" → "порт-выход". Согласно ТЗ, чтобы
 * соединить двух абонентов, телефонистка кликает гаечным ключом сперва по
 * входу (загорается), затем по выходу — маршрут установлен. Повторный клик
 * по уже занятому входу разрывает соединение.
 * <p>
 * Хранится в памяти на блок-сущности станции (см. {@link StationBlockEntity})
 * и сериализуется в NBT, чтобы маршруты переживали перезапуск сервера.
 */
public class RoutingManager {

    /** Порт-вход, который телефонистка выбрала первым и ждёт клика по выходу. */
    private PortRef pendingInput;

    /** in -> out. Каждый порт может быть входом только одного маршрута. */
    private final Map<PortRef, PortRef> routes = new HashMap<>();

    public PortRef getPendingInput() {
        return pendingInput;
    }

    public void setPendingInput(PortRef input) {
        this.pendingInput = input;
    }

    public void clearPending() {
        this.pendingInput = null;
    }

    /**
     * Обрабатывает клик гаечным ключом по гнезду в режиме маршрутизации.
     *
     * @return результат клика — что произошло, для сообщения игроку и синхронизации звука.
     */
    public RouteClickResult handleWrenchClick(PortRef clicked) {
        // Клик по гнезду, которое уже является входом активного маршрута — разрыв соединения.
        if (routes.containsKey(clicked)) {
            PortRef out = routes.remove(clicked);
            if (Objects.equals(pendingInput, clicked)) {
                pendingInput = null;
            }
            return RouteClickResult.disconnected(clicked, out);
        }

        if (pendingInput == null) {
            pendingInput = clicked;
            return RouteClickResult.pendingSet(clicked);
        }

        if (pendingInput.equals(clicked)) {
            // Повторный клик по тому же гнезду — отмена выбора.
            pendingInput = null;
            return RouteClickResult.pendingCancelled(clicked);
        }

        PortRef in = pendingInput;
        PortRef out = clicked;
        pendingInput = null;
        routes.put(in, out);
        return RouteClickResult.connected(in, out);
    }

    public PortRef getRouteOutput(PortRef input) {
        return routes.get(input);
    }

    public boolean isRouted(PortRef port) {
        return routes.containsKey(port);
    }

    public int getActiveRouteCount() {
        return routes.size();
    }

    public Map<PortRef, PortRef> getAllRoutes() {
        return routes;
    }

    public void removeRoutesInvolving(PortRef port) {
        routes.remove(port);
        routes.values().removeIf(port::equals);
        if (Objects.equals(pendingInput, port)) {
            pendingInput = null;
        }
    }

    /** Результат обработки клика ключом — используется для сообщений игроку и звука. */
    public record RouteClickResult(Type type, PortRef a, PortRef b) {
        public enum Type { PENDING_SET, PENDING_CANCELLED, CONNECTED, DISCONNECTED }

        public static RouteClickResult pendingSet(PortRef port) {
            return new RouteClickResult(Type.PENDING_SET, port, null);
        }

        public static RouteClickResult pendingCancelled(PortRef port) {
            return new RouteClickResult(Type.PENDING_CANCELLED, port, null);
        }

        public static RouteClickResult connected(PortRef in, PortRef out) {
            return new RouteClickResult(Type.CONNECTED, in, out);
        }

        public static RouteClickResult disconnected(PortRef in, PortRef out) {
            return new RouteClickResult(Type.DISCONNECTED, in, out);
        }
    }
}
