package tropical.client.features.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import tropical.client.features.waypoint.WaypointScreen.Utils;

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

            dimension = Utils.makeDimensionWidget(200, 20);
        }

        EditBox name;
        EditBox xbox, ybox, zbox;
        CycleButton<String> dimension;
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

        private void addWaypoint() {
            float x, y, z;
            try {
                x = Float.valueOf(this.xbox.getValue());
                y = Float.valueOf(this.ybox.getValue());
                z = Float.valueOf(this.zbox.getValue());
            } catch (Exception e) {
                return;
            }


            Waypoint wp = WaypointManager.addWaypoint(name.getValue(), dimension.getValue(), x, y, z);
            if (wp == null) {
                return;
            }

            this.onClose();
        }

        public void copy(Waypoint wp) {
            this.setCoords(wp.asVec3());
            this.dimension.setValue(wp.dimension);
            this.name.setValue("Copy of - " + wp.name);
        }

        private void autoFill() {
            Vec3 pos = new Vec3(client.player.xOld, client.player.yOld, client.player.zOld);
            this.setCoords(pos);
            this.dimension.setValue(WaypointManager.getCurrentDimensionString());
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

