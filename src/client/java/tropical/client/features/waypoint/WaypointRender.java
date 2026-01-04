package tropical.client.features.waypoint;

import java.util.ArrayList;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import com.mojang.blaze3d.platform.Window;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import tropical.client.TropicalClient;
import tropical.client.TropicalUtils;

public class WaypointRender {
    public static void initalize() {
        Identifier id = Identifier.fromNamespaceAndPath(TropicalClient.MOD_ID, "before_chat");
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR, id, WaypointRender::renderWaypoints);
    }

    public static void renderWaypoints(GuiGraphics context, DeltaTracker tick) {
        Minecraft client = Minecraft.getInstance();
        ArrayList<Waypoint> wps = WaypointManager.getWaypoints();
        for (Waypoint wp : wps) {
            if (!wp.enabled || wp.dimension != TropicalUtils.currentDimension) continue;
            renderWaypoint(wp, client, context, tick);
        }
    }

    private static void renderWaypoint(Waypoint wp, Minecraft client, GuiGraphics context, DeltaTracker tick) {
        Matrix3x2fStack matrices = context.pose();

        GameRenderer renderer = client.gameRenderer;

        Window window = client.getWindow();
        int scaledWidth = window.getGuiScaledWidth();
        int scaledHeight = window.getGuiScaledHeight();

        Vec3 wpVec = wp.asVec3().add(0.5, 1, 0.5);
        Vec3 v = renderer.projectPointToScreen(wpVec);

        if (v.z >= 1d) return;

        matrices.pushMatrix();

        float x = ((float)v.x * 0.5f + 0.5f) * (float)scaledWidth;
        float y = (1f - ((float)v.y * 0.5f + 0.5f)) * (float)scaledHeight;
        matrices.translate(x, y);
        context.drawString(client.font, wp.name, 0, 0, 0xFFFFFFFF, true);

        matrices.popMatrix();
    }
}
