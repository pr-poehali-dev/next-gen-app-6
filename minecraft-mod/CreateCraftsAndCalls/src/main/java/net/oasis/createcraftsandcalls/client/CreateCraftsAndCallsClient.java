package net.oasis.createcraftsandcalls.client;

import com.mrh0.createaddition.rendering.WireNodeRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.oasis.createcraftsandcalls.CreateCraftsAndCalls;
import net.oasis.createcraftsandcalls.registry.ModBlockEntities;

/**
 * Клиентская инициализация мода. Подключает готовый {@link WireNodeRenderer}
 * из Create: Crafts & Additions к нашим блок-сущностям телефона и станции —
 * благодаря тому, что обе реализуют {@code IWireNode}, провода между ними
 * рисуются той же механикой, что и обычные медные провода C&A, включая
 * подсветку (см. ТЗ п.7 "визуальные требования").
 */
@Mod(value = CreateCraftsAndCalls.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateCraftsAndCalls.MOD_ID, value = Dist.CLIENT)
public class CreateCraftsAndCallsClient {

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PHONE.get(), WireNodeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STATION.get(), net.oasis.createcraftsandcalls.client.StationLabelRenderer::new);
    }
}