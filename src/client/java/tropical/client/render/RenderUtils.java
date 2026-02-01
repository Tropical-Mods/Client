package tropical.client.render;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class RenderUtils {
    static Minecraft client = Minecraft.getInstance();
    public static float zOffset = 1f;
    public static Vec2 convertPointToScreen(Vec3 point, int squareLen, int x, int y) {
        Vec2 projected = project(point);

        return new Vec2((projected.x+1)/2 * (squareLen),
                        (1-(projected.y+1)/2) * (squareLen)).add(new Vec2(x, y));
    }

    public static Vec2 convertPointToScreen(Vec3 point, int squareLen) {
        return convertPointToScreen(point, squareLen, 0, 0);
    }

    private static Vec2 project(Vec3 point) {
        return new Vec2((float)(point.x/(point.z+zOffset)), (float)(point.y/(point.z+zOffset)));
    }

    public static Vec3 rotateYZ(Vec3 point, double angle) {
        double c = Mth.cos(angle);
        double s = Mth.sin(angle);
        
        return new Vec3(point.x, (point.y * c)-(point.z * s), (point.y * s)+(point.z * c));
    }

    public static Vec3 rotateXZ(Vec3 point, double angle) {
        double c = Mth.cos(angle);
        double s = Mth.sin(angle);

        return new Vec3((point.x*c)-(point.z*s), point.y, (point.x*s)+(point.z*c));
    }

    public static void drawLine2D(GuiGraphics context, float x1, float y1, float x2, float y2, int scale, int color) {
        Matrix3x2fStack matrices = context.pose();

        float x = (x1) * scale;
        float y = (y1) * scale;
        float w = ((x2-x1)) * scale;
        float h = ((y2-y1)) * scale;

        float radian = (float)Mth.atan2(h, w);
        int length = Math.round(Mth.sqrt(w*w + h*h));

        matrices.pushMatrix();

        matrices.scale(1f / scale);
        matrices.translate(x, y);
        matrices.rotate(radian);
        matrices.translate(-0.5f, -0.5f);
        context.hLine(0, length, 0, color);

        matrices.popMatrix();
    }

}
