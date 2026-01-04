package tropical.client.features.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import tropical.client.TropicalUtils;
import tropical.client.TropicalUtils.Dimension;

public class DimensionButton extends Button {
    Minecraft client;
    Font font;
    TropicalUtils.Dimension dimension;
    public DimensionButton(int x, int y, int width, int height, Component c, OnPress onPress) {
        super(x, y, width, height, c, onPress, Button.DEFAULT_NARRATION);
        this.client = Minecraft.getInstance();
        this.font = client.font;
        this.dimension = TropicalUtils.getCurrentDimension();
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        Dimension[] values = TropicalUtils.Dimension.values();
        this.dimension = values[(this.dimension.ordinal() + 1) % values.length];

        super.onClick(mouseButtonEvent, bl);
    }

    @Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
        context.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFF888888);
        int yCenter = this.getY() + (this.height/2) - (this.font.lineHeight / 2);
        int xCenter = this.getX() + (this.width / 2);
        context.drawCenteredString(this.font, this.dimension.toString(), xCenter, yCenter, 0xFFFFFFFF);
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public void setDimension(Dimension d) {
        this.dimension = d;
    }
}
