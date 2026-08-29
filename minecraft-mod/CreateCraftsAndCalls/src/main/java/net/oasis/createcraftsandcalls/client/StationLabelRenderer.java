package net.oasis.createcraftsandcalls.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.rendering.WireNodeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.oasis.createcraftsandcalls.station.StationBlockEntity;
import net.oasis.createcraftsandcalls.station.StationGroup;
import net.oasis.createcraftsandcalls.telephony.PortRef;
import org.joml.Matrix4f;

/**
 * Рисует провода станции (делегируя в готовый {@link WireNodeRenderer} из
 * Create: Crafts & Additions) и подписи над каждым гнездом — какой игрок
 * подключён к этому порту, согласно ТЗ п.3.2 "маркировка портов".
 */
public class StationLabelRenderer implements BlockEntityRenderer<StationBlockEntity> {

    private final WireNodeRenderer<StationBlockEntity> wireDelegate;
    private final Font font;

    public StationLabelRenderer(BlockEntityRendererProvider.Context context) {
        this.wireDelegate = new WireNodeRenderer<>(context);
        this.font = context.getFont();
    }

    @Override
    public void render(StationBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        wireDelegate.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        if (blockEntity.getLevel() == null) {
            return;
        }

        for (int local = 0; local < StationBlockEntity.PORTS_PER_BLOCK; local++) {
            String label = resolvePortLabel(blockEntity, local);
            if (label == null) {
                continue;
            }
            renderLabel(blockEntity, local, label, poseStack, bufferSource, packedLight);
        }
    }

    private String resolvePortLabel(StationBlockEntity station, int localPort) {
        var level = station.getLevel();
        var phonePos = station.getPhoneBoundToPort(
                StationGroup.globalPortIndex(level, station.getBlockPos(), localPort));
        if (phonePos == null) {
            return null;
        }
        if (!(level.getBlockEntity(phonePos) instanceof net.oasis.createcraftsandcalls.blockentity.PhoneBlockEntity phone)) {
            return null;
        }
        var boundPlayer = phone.getBoundPlayer();
        if (boundPlayer == null) {
            return "?";
        }
        Player player = level.getPlayerByUUID(boundPlayer);
        String name = player != null ? player.getGameProfile().getName() : boundPlayer.toString().substring(0, 8);

        int globalPort = StationGroup.globalPortIndex(level, station.getBlockPos(), localPort);
        PortRef myRef = new PortRef(station.getGroupOrigin(), globalPort);
        boolean routed = station.getRouting().isRouted(myRef)
                || station.getRouting().getAllRoutes().containsValue(myRef);

        return routed ? "§a" + name : "§7" + name;
    }

    private void renderLabel(StationBlockEntity station, int localPort, String label, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight) {
        var offset = station.getNodeOffset(localPort);

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y + 0.35, offset.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.02F, -0.02F, 0.02F);

        Matrix4f matrix = poseStack.last().pose();
        Component text = Component.literal(label);
        float x = -font.width(text) / 2f;
        font.drawInBatch(text, x, 0, 0xFFFFFF, false, matrix, bufferSource,
                Font.DisplayMode.SEE_THROUGH, 0, packedLight);

        poseStack.popPose();
    }
}
