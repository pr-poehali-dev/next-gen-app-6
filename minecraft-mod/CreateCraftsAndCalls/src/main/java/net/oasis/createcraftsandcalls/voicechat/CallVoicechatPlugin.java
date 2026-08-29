package net.oasis.createcraftsandcalls.voicechat;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;

/**
 * Плагин для Simple Voice Chat. SVC сам находит этот класс через сканирование
 * аннотаций у себя на старте (никакой ручной регистрации не требуется) —
 * достаточно пометить класс {@link ForgeVoicechatPlugin} и реализовать
 * {@link VoicechatPlugin}. Если сам мод Simple Voice Chat не установлен на
 * сервере, этот класс просто никогда не будет загружен и использован — мод
 * Create Crafts & Calls от этого не ломается (см. optional-зависимость в
 * neoforge.mods.toml).
 */
@ForgeVoicechatPlugin
public class CallVoicechatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return CreateCraftsAndCalls.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi serverApi) {
            VoicechatBridge.setServerApi(serverApi);
            CreateCraftsAndCalls.LOGGER.info("Create Crafts & Calls: подключение к Simple Voice Chat установлено.");
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        VoicechatBridge.setServerApi(event.getVoicechat());
    }
}
