package tropical.client.features.waypoint;

import java.util.ArrayList;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec2;
import tropical.client.Render.Mesh;
import tropical.client.Render.MeshManager;
import tropical.client.features.waypoint.WaypointScreen.WaypointListEntry;

public class WaypointMapWidget extends AbstractWidget {
    private int color = 0xFF888888;
    private ArrayList<WaypointListEntry> entries;
    private int radius;
    private int scale;

    private static int blocksRadius = 1000;

    private float[][] circleMesh = new float[361][2];
    private Mesh defaultMesh;

    public WaypointMapWidget(int i, int j, int k, int l, Component component, ArrayList<WaypointListEntry> wps) {
        super(i, j, k, l, component);
        this.entries = wps;

        this.radius = Math.min(this.height, this.width);
        this.scale = Minecraft.getInstance().getWindow().getGuiScale();
        this.defaultMesh = MeshManager.getDefaultMesh();

        //double yz = 0 - (Math.PI/5d);
        //this.defaultMesh.permRotateYZ(yz);

        //this.defaultMesh.loadScreenPoints(this.radius);
        this.loadCricleMesh();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
       this.defaultButtonNarrationText(narrationElementOutput);
    }

    private void loadCricleMesh() {
        for (int i = 0; i < this.circleMesh.length; i++) {
            double radian = i * (Math.PI/180);

            this.circleMesh[i][0] = (float) ((this.radius/2d) * Math.cos(radian)) + ((this.width/2f));
            this.circleMesh[i][1] = (float) ((this.radius/2d) * Math.sin(radian)) + ((this.height/2f)); 
        }
    }

    private void drawCircle(GuiGraphics context) {
        for (int i = 1; i < this.circleMesh.length; i++) {
            this.drawLine2D(context,
                circleMesh[i][0] + this.getX(), circleMesh[i][1] + this.getY(),
                circleMesh[i-1][0] + this.getX(), circleMesh[i-1][1] + this.getY()
            );
        }
    }

    private void drawWaypointLine(GuiGraphics context, Vec2 dir) {
        Matrix3x2fStack matrices = context.pose();

        int length = 0;
        if (dir.length() > WaypointMapWidget.blocksRadius) {
            length = this.radius;
        } else {
            length = (int) ( ( (dir.length()) / (WaypointMapWidget.blocksRadius) ) * this.radius );
        }

        float x = ((this.width / 2.0f)+this.getX()) * scale;
        float y = ((this.height / 2.0f)+this.getY()) * scale;

        float radian = (float)Mth.atan2((double)dir.y, (double)dir.x) - (float)Math.PI;

        matrices.pushMatrix();

        matrices.scale(1f / scale);
        matrices.translate(x, y);
        matrices.rotate(radian);
        matrices.translate(-0.5f, -0.5f);
        context.hLine(0, length, 0, color);

        matrices.popMatrix();
    }

    private void drawLine2D(GuiGraphics context, float x1, float y1, float x2, float y2) {
        Matrix3x2fStack matrices = context.pose();

        float x = (x1) * this.scale;
        float y = (y1) * this.scale;
        float w = ((x2-x1)) * this.scale;
        float h = ((y2-y1)) * this.scale;

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

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        //for (WaypointListEntry entry : this.entries) {
        //    this.drawWaypointLine(context, entry.dir()); 
        //}

        //this.drawCircle(context);
        //
        double dt = 1d / 60d;
        this.defaultMesh.renderAndRotate(context, this.scale, this.radius,
            this.getX() + ((this.width/2) - (this.radius/2)), this.getY(), dt);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        return;
    }

    @Override
    public void onDrag(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        WaypointMapWidget.blocksRadius += (deltaY * 2);
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        MeshManager.cycleMesh();
        this.defaultMesh = MeshManager.getDefaultMesh();
    }
}
