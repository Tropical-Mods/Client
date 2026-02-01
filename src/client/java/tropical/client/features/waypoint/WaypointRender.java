package tropical.client.features.waypoint;

import java.util.ArrayList;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
import tropical.client.mixin.client.GameRendererAccessor;

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

        var camera = renderer.getMainCamera();

        //this is a copy of GameRenderer.projectPointToScreen
        float tickDelta = tick.getGameTimeDeltaPartialTick(true);
        double fov = ((GameRendererAccessor) renderer).invokeGetFov(camera, tickDelta, true);
		Matrix4f projectionMatrix = renderer.getProjectionMatrix((float) fov);
		Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
		Matrix4f viewMatrix = new Matrix4f().rotation(quaternionf);
		Matrix4f transformMatrix = projectionMatrix.mul(viewMatrix);
		Vec3 cameraPos = camera.position();
		Vec3 eyePlayerPos = wpVec.subtract(cameraPos);
		Vector3f vector3f = transformMatrix.transformProject(eyePlayerPos.toVector3f());
		Vec3 ndcPos = new Vec3(vector3f);

        if (ndcPos.z >= 1d) return;

        matrices.pushMatrix();

        float x = ((float)ndcPos.x * 0.5f + 0.5f) * (float)scaledWidth;
        float y = (1f - ((float)ndcPos.y * 0.5f + 0.5f)) * (float)scaledHeight;
        matrices.translate(x, y);
        float textWidth = client.font.width(wp.name);
        int textHeight = (int)client.font.lineHeight;
        int xOffset = (int)(textWidth / 2f);
        int margin = 2;
        context.fill(
            0 - margin - xOffset,
            0 - margin,
            xOffset + margin,
            textHeight + margin,
            0x44888888
        );

        context.drawString(client.font, wp.name, 0 - xOffset, 0, 0xFFFFFFFF, true);

        matrices.popMatrix();
    }
}
