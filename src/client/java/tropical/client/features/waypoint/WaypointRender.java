package tropical.client.features.waypoint;

import java.util.ArrayList;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.Window;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import tropical.client.TropicalClient;

public class WaypointRender {
    public static void initalize() {
        Identifier id = Identifier.fromNamespaceAndPath(TropicalClient.MOD_ID, "before_chat");
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id, WaypointRender::renderWaypoints);
    }

    public static void renderWaypoints(GuiGraphics context, DeltaTracker tick) {
        Minecraft client = Minecraft.getInstance();
        ArrayList<Waypoint> wps = WaypointManager.getWaypoints();
        for (Waypoint wp : wps) {
            if (!wp.enabled) continue;
            renderWaypoint(wp, client, context, tick);
        }
    }

    private static void renderWaypoint(Waypoint wp, Minecraft client, GuiGraphics context, DeltaTracker tick) {
        GameRenderer renderer = client.gameRenderer;

        Window window = client.getWindow();
        int scaledWidth = window.getGuiScaledWidth();
        int scaledHeight = window.getGuiScaledHeight();

        Vec3 v = renderer.projectPointToScreen(wp.asVec3());

        if (v.z >= 1d) return;

        int scaledX = (int) ((v.x * 0.5f + 0.5f) * scaledWidth);
        int scaledY = (int) ((1f - (v.y * 0.5f + 0.5f)) * scaledHeight);

        context.drawString(client.font, wp.name, scaledX, scaledY, 0xFFFFFFFF, true);
    }
}
