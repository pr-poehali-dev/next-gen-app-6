package net.oasis.createcraftsandcalls.voicechat;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Создаёт и удаляет временные "закрытые" группы Simple Voice Chat для
 * телефонных звонков. Каждый звонок между двумя абонентами — это отдельная
 * скрытая группа типа ISOLATED (её не видно в списке групп, в неё нельзя
 * зайти вручную) — ровно на время, пока маршрут на станции замкнут.
 */
public class CallGroupManager {

    /** UUID игрока -> UUID временной группы звонка, в которую он сейчас помещён. */
    private static final Map<UUID, UUID> PLAYER_GROUPS = new HashMap<>();
    /** UUID группы -> предыдущая группа игрока (чтобы вернуть на место после звонка, если была). */
    private static final Map<UUID, UUID> PREVIOUS_GROUP = new HashMap<>();

    private CallGroupManager() {
    }

    /**
     * Соединяет двух игроков временным голосовым каналом. Если один из них
     * не подключён к Simple Voice Chat — звонок всё равно считается
     * установленным на уровне станции, просто звук не пойдёт.
     */
    public static void connect(VoicechatServerApi api, UUID playerA, UUID playerB) {
        VoicechatConnection connA = api.getConnectionOf(playerA);
        VoicechatConnection connB = api.getConnectionOf(playerB);
        if (connA == null || connB == null) {
            return;
        }

        Group callGroup = api.groupBuilder()
                .setName("call-" + playerA + "-" + playerB)
                .setType(Group.Type.ISOLATED)
                .setHidden(true)
                .setPersistent(false)
                .build();

        rememberPrevious(playerA, connA);
        rememberPrevious(playerB, connB);

        connA.setGroup(callGroup);
        connB.setGroup(callGroup);

        PLAYER_GROUPS.put(playerA, callGroup.getId());
        PLAYER_GROUPS.put(playerB, callGroup.getId());
    }

    private static void rememberPrevious(UUID player, VoicechatConnection connection) {
        Group current = connection.getGroup();
        if (current != null) {
            PREVIOUS_GROUP.put(player, current.getId());
        } else {
            PREVIOUS_GROUP.remove(player);
        }
    }

    /**
     * Разрывает звонок для одного игрока: убирает его из временной группы
     * (и удаляет саму группу, если она опустела), возвращает предыдущую
     * группу, если она была.
     */
    public static void disconnect(VoicechatServerApi api, UUID player) {
        UUID groupId = PLAYER_GROUPS.remove(player);
        if (groupId == null) {
            return;
        }

        VoicechatConnection connection = api.getConnectionOf(player);
        if (connection != null) {
            UUID previous = PREVIOUS_GROUP.remove(player);
            if (previous != null) {
                Group previousGroup = api.getGroup(previous);
                connection.setGroup(previousGroup);
            } else {
                connection.setGroup(null);
            }
        }

        boolean stillUsed = PLAYER_GROUPS.containsValue(groupId);
        if (!stillUsed) {
            api.removeGroup(groupId);
        }
    }

    public static boolean isInCall(UUID player) {
        return PLAYER_GROUPS.containsKey(player);
    }
}
