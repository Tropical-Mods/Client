package tropical.client.Render;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Mesh {
    public Vec3[] vertices;
    public int[][] faces;
    public Vec2[] screenPoints;
    public Mesh(Vec3[] vertices, int[][] faces) {
        this.vertices = vertices;
        this.faces = faces;

        RenderUtils.zOffset = this.getZOffset();
    }

    public void loadScreenPoints(int squareLen) {
        ArrayList<Vec2> sp = new ArrayList<>();
        for (int i = 0; i < this.vertices.length; i++) {
            sp.add(i, RenderUtils.convertPointToScreen(vertices[i], squareLen));
        }

        this.screenPoints = sp.toArray(new Vec2[0]);
    }

    public void permRotateYZ(double angle) {
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = RenderUtils.rotateYZ(vertices[i], angle);
        }
    }

    private float getZOffset() {
        double currentWorstOffset = 0;
        for (int i = 0; i < vertices.length; i++) {
            Vec3 vertex = vertices[i];

            double offset = Math.max((vertex.x-vertex.z), (vertex.y - vertex.z));
            if (offset > currentWorstOffset) currentWorstOffset = offset;
        }

        return (float)currentWorstOffset;
    }

    double angle = 0;
    double yzAngle = 0 - (Math.PI/5);
    public void renderAndRotate(GuiGraphics context, int scale, int squareLen, int x, int y, double dt) {
        //yzAngle += Math.PI*(dt * 0.1);
        angle += Math.PI*dt*(0.1);
        for (int i = 0; i < faces.length; i++) {
            for (int j = 0; j < faces[i].length; j++) {
                Vec3 a = RenderUtils.rotateYZ(
                        RenderUtils.rotateXZ(vertices[faces[i][j] - 1], angle), yzAngle);

                Vec3 b = RenderUtils.rotateYZ(
                        RenderUtils.rotateXZ(vertices[faces[i][(j + 1)%faces[i].length] - 1], angle), yzAngle);

                //Vec3 a = RenderUtils.rotateXZ(vertices[faces[i][j] - 1], angle);

                //Vec3 b = RenderUtils.rotateXZ(vertices[faces[i][(j + 1)%faces[i].length] - 1], angle);

                Vec2 start = RenderUtils.convertPointToScreen(a, squareLen, x, y);
                Vec2 end = RenderUtils.convertPointToScreen(b, squareLen, x, y);

                RenderUtils.drawLine2D(context, start.x, start.y, end.x, end.y, scale, 0xFFFF0000);
            }
        }

        //context.renderOutline(x, y, squareLen, squareLen, 0xFF888888);
    }

    public void render(GuiGraphics context, int scale) {
        for (int i = 0; i < faces.length; i++) {
            renderFace(context, scale, faces[i]);
        }
    }

    private void renderFace(GuiGraphics context, int scale, int[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            Vec2 a = screenPoints[vertices[i] - 1];
            Vec2 b = screenPoints[vertices[(i+1)%vertices.length] - 1];

            RenderUtils.drawLine2D(context, a.x, a.y, b.x, b.y, scale, 0xFFFF0000);
        }
    }
}
