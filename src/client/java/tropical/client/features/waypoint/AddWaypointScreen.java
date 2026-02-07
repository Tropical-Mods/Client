package tropical.client.features.waypoint;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import tropical.client.TropicalUtils;

public class AddWaypointScreen extends Screen {
    private Screen parent;
    private Minecraft client;
    public AddWaypointScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
        this.client = Minecraft.getInstance();

        name = new EditBox(client.font, 0, 0, 200, 20, Component.empty());

        xbox = new EditBox(client.font, 60, 20, Component.empty());
        ybox = new EditBox(client.font, 60, 20, Component.empty());
        zbox = new EditBox(client.font, 60, 20, Component.empty());

        dimension = new DimensionButton(0, 0, 200, 20, Component.empty(), (btn) -> {
            updateTranslateButton();
        });
    }

    EditBox name;
    EditBox xbox, ybox, zbox;
    DimensionButton dimension;
    Button translateButton;
    public void init() {
        LinearLayout layout = LinearLayout.vertical().spacing(4);
        layout.defaultCellSetting().alignHorizontallyCenter();

        layout.addChild(name);

        LinearLayout coords = LinearLayout.horizontal().spacing(4);
        coords.defaultCellSetting().alignHorizontallyCenter();
        
        coords.addChild(xbox);
        coords.addChild(ybox);
        coords.addChild(zbox);

        coords.arrangeElements();
        layout.addChild(coords);

        layout.addChild(dimension);

        String oppositeStr = this.getOppositeDimension(this.dimension.getDimension()).simple();
        this.translateButton = layout.addChild(Button.builder(Component.nullToEmpty("Translate to " + oppositeStr), (btn) -> {
            this.translate();
        }).size(200, 20).build());

        LinearLayout buttonsLayer = LinearLayout.horizontal().spacing(4);
        buttonsLayer.addChild(Button.builder(Component.nullToEmpty("Save"), (btn) -> {
            this.addWaypoint();                
        }).size(65, 20).build());

        buttonsLayer.addChild(Button.builder(Component.nullToEmpty("Defaults"), (btn) -> {
            this.autoFill();
        }).size(65, 20).build());

        buttonsLayer.addChild(Button.builder(Component.nullToEmpty("Cancel"), (btn) -> {
            this.onClose();
        }).size(65, 20).build());

        layout.addChild(buttonsLayer);

        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, 0, 0, this.width, this.height);
        layout.visitWidgets( (widget) -> {
            this.addRenderableWidget(widget);
        } );
    }

    private TropicalUtils.Dimension getOppositeDimension(TropicalUtils.Dimension dimension) {
        switch(dimension) {
            case TropicalUtils.Dimension.OVERWORLD: {
                return TropicalUtils.Dimension.NETHER;
            }

            default: {
                return TropicalUtils.Dimension.OVERWORLD;
            }
        }
    }

    private void updateTranslateButton() {
        if (this.translateButton == null) return;

        String oppositeStr = getOppositeDimension(this.dimension.getDimension()).simple();
        this.translateButton.setMessage(Component.nullToEmpty("Translate to " + oppositeStr));
    }

    private void translate() {
        Vec3 newCoords;
        Vec3 currentCoords = this.getCoords();
        if (currentCoords == null) {
            return;
        }

        TropicalUtils.Dimension currentDimension = this.dimension.getDimension();
        TropicalUtils.Dimension opposite = getOppositeDimension(currentDimension);
        switch (opposite) {
            case TropicalUtils.Dimension.NETHER: {
                newCoords = currentCoords.multiply(8d, 1d, 8d);
                break;
            }

            case TropicalUtils.Dimension.OVERWORLD: {
                newCoords = currentCoords.multiply(1d/8d, 1d, 1d/8d);
                break;
            }

            default: {
                newCoords = currentCoords.multiply(1d, 1d, 1d);
                break;
            }
        }

        this.setCoords(newCoords);
        this.dimension.setDimension(opposite);
        this.updateTranslateButton();
    }

    private void addWaypoint() {
        float x, y, z;
        try {
            x = Float.valueOf(this.xbox.getValue());
            y = Float.valueOf(this.ybox.getValue());
            z = Float.valueOf(this.zbox.getValue());
        } catch (Exception e) {
            return;
        }

        Waypoint wp = WaypointManager.addWaypoint(
            name.getValue(),
            dimension.getDimension().toString(),
            x, y, z,
            (0xFF<<24));
        if (wp == null) {
            return;
        }

        this.onClose();
    }

    @Nullable
    private Vec3 getCoords() {
        float x, y, z;
        try {
            x = Float.valueOf(this.xbox.getValue());
            y = Float.valueOf(this.ybox.getValue());
            z = Float.valueOf(this.zbox.getValue());
        } catch (Exception e) {
            return null;
        }

        return new Vec3(x, y, z);
    }

    public void copy(Waypoint wp) {
        this.setCoords(wp.asVec3());
        this.dimension.setDimension(wp.dimension);
        this.name.setValue("Copy of - " + wp.name);
    }

    private void autoFill() {
        Vec3 pos = new Vec3(client.player.xOld, client.player.yOld, client.player.zOld);
        this.setCoords(pos);
        this.dimension.setDimension(TropicalUtils.getCurrentDimension());
    }

    private void setCoords(Vec3 pos) {
        this.xbox.setValue(String.valueOf((float)pos.x));
        this.xbox.setMessage(Component.nullToEmpty(xbox.getValue()));

        this.ybox.setValue(String.valueOf((float)pos.y));
        this.ybox.setMessage(Component.nullToEmpty(ybox.getValue()));

        this.zbox.setValue(String.valueOf((float)pos.z));
        this.zbox.setMessage(Component.nullToEmpty(zbox.getValue()));
    }

    @Override
    public void onClose() {
        client.setScreen(this.parent);
    }
}

