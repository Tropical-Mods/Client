package tropical.client.features.waypoint;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class WaypointScreen extends Screen {
    public record WaypointListEntry(FocusableTextWidget widget, Waypoint wp) {}

    //private LinearLayout layout;
    private ArrayList<WaypointListEntry> entries;
    private Minecraft client;
    private HeaderAndFooterLayout lay;
    private @Nullable Waypoint currentFocused = null;
    public Screen parent;
    public WaypointScreen(Screen parent) {
        super(Component.nullToEmpty("Waypoints"));
        this.parent = parent;
        this.client = Minecraft.getInstance();
        this.lay = new HeaderAndFooterLayout(this, 49, 60);
        //this.layout = LinearLayout.vertical().spacing(4);
    }

    @Override
    protected void init() {
        this.l();
    }

    public void l() {
        LinearLayout headerLayout = this.lay.addToHeader(LinearLayout.vertical().spacing(4));
        headerLayout.defaultCellSetting().alignHorizontallyCenter();
        headerLayout.addChild(new StringWidget(this.title, this.font));

        headerLayout.arrangeElements();

        ScrollableLayout list = this.makeWaypointsList(this.lay.getContentHeight(),
                                lay.getFooterHeight(), lay.getHeaderHeight());

        this.lay.addToContents(list);
        this.makeFooter();

        this.lay.visitWidgets( (widget) -> {
            this.addRenderableWidget(widget);
        } );
    }

    private ScrollableLayout makeWaypointsList(int contentHeight, int footerHeight, int headerHeight) {
        LinearLayout layout = LinearLayout.vertical().spacing(4);
        this.entries = new ArrayList<>();
        var wps = WaypointManager.getWaypoints();
        for (int i = 0; i < wps.size(); i++) {
            LinearLayout row = LinearLayout.horizontal().spacing(4);
            Waypoint wp = wps.get(i);
            String line = this.getWaypointBrief(wp);
            Font font = Minecraft.getInstance().font;
            FocusableTextWidget w = FocusableTextWidget.builder(Component.nullToEmpty(line), font).build();

            int x = 10;
            int y = w.getHeight() * (i+1);
            w.setX(x);
            w.setY(y);

            WaypointButton toggleButton = new WaypointButton(
                x + w.getWidth(), y, 10, 10,
                Component.nullToEmpty("nothing"), wp,
                (waypoint) -> {
                    WaypointManager.toggleWaypoint(waypoint);
                }
            );

            row.addChild(w);
            row.addChild(toggleButton);

            layout.addChild(row);
            this.entries.add(new WaypointListEntry(w, wp));
        }

        layout.arrangeElements();

        ScrollableLayout scroll = new ScrollableLayout(Minecraft.getInstance(), layout, 10);
        scroll.setMaxHeight(contentHeight);
        scroll.setMinWidth(this.width - 20);
        scroll.setX(0);
        scroll.setY(headerHeight);
        scroll.arrangeElements();

        return scroll;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (int i = 0; i < this.entries.size(); i++) {
            var entry = entries.get(i);

            if (entry.wp == null) {
                this.reloadScreen();
                return;
            }

            if (!entry.wp.enabled) {
                entry.widget.setAlpha(0.5f);
            } else {
                entry.widget.setAlpha(1.0f);
            }

            if (entry.widget.isFocused()) {
                if (this.currentFocused == null) {
                    this.currentFocused = entry.wp;
                    this.updateFocusedWaypoint();
                } else if (this.currentFocused != null && this.currentFocused.id != entry.wp.id) {
                    this.currentFocused = entry.wp;
                    this.updateFocusedWaypoint();
                }
            }
        }
    }


    private void reloadScreen() {
        this.clearWidgets();
        this.l();
    }

    EditBox nameBox;
    EditBox xbox, zbox, ybox;
    private void makeFooter() {
        LinearLayout infoSide = this.lay.addToFooter(LinearLayout.vertical().spacing(4));
        infoSide.defaultCellSetting().alignHorizontallyCenter();

        LinearLayout coords = LinearLayout.horizontal().spacing(4);

        this.nameBox = infoSide.addChild(new EditBox(this.client.font, 0, 0, 100, 20, Component.empty()));

        this.xbox = coords.addChild(new EditBox(this.client.font, 0, 0, 35, 20, Component.empty()));
        this.ybox = coords.addChild(new EditBox(this.client.font, 0, 0, 35, 20, Component.empty()));
        this.zbox = coords.addChild(new EditBox(this.client.font, 0, 0, 35, 20, Component.empty()));

        var bt = Utils.makeDimensionWidget();
        coords.addChild(bt);

        coords.arrangeElements();

        infoSide.defaultCellSetting().alignHorizontallyCenter();
        infoSide.addChild(coords);
        infoSide.arrangeElements();
        infoSide.setY(this.height - this.lay.getFooterHeight() + 10);
        infoSide.setX(10);

        GridLayout buttonGrid = this.lay.addToFooter(new GridLayout(2, 2).rowSpacing(4).columnSpacing(4));
        buttonGrid.addChild(Button.builder(Component.nullToEmpty("Save Changes"), (btn) -> {
            this.updateWaypoint();
        }).size(100, 20).build(), 1, 1);
        
        buttonGrid.addChild(Button.builder(Component.nullToEmpty("Delete"), (btn) -> {
            this.removeWaypoint();
        }).size(100, 20).build(), 1, 2);

        buttonGrid.addChild(Button.builder(Component.nullToEmpty("Copy"), (btn) -> {
            this.copyWaypoint(); 
        }).size(100, 20).build(), 2, 1);

        buttonGrid.addChild(Button.builder(Component.nullToEmpty("New"), (btn) -> {
            this.addWaypoint(); 
        }).size(100, 20).build(), 2, 2);

        buttonGrid.arrangeElements();
        buttonGrid.setY(this.height - this.lay.getFooterHeight() + 10);
        buttonGrid.setX(this.width - 10 - buttonGrid.getWidth());
    }

    private void copyWaypoint() {
        AddWaypointScreen addScreen = new AddWaypointScreen(this); 
        addScreen.copy(this.currentFocused);

        this.client.setScreen(addScreen);
    }

    private void addWaypoint() {
        AddWaypointScreen addScreen = new AddWaypointScreen(this);
        this.client.setScreen(addScreen);
    }

    private void removeWaypoint() {
        Waypoint wp = WaypointManager.removeWaypoint(this.currentFocused);
        if (wp == null) {
            return;
        }

        this.currentFocused = null;
        this.reloadScreen();
    }

    private void updateWaypoint() {
        Waypoint newWp = this.currentFocused.copy();

        newWp.name = this.nameBox.getValue();
        try {
            newWp.x = Float.parseFloat(this.xbox.getValue());
            newWp.y = Float.parseFloat(this.ybox.getValue());
            newWp.z = Float.parseFloat(this.zbox.getValue());
        } catch (Exception e) {
            return;
        }

        boolean changed = (!newWp.name.equals(currentFocused.name)) ||
                          (newWp.x != currentFocused.x) || (newWp.y != currentFocused.y) ||
                          (newWp.z != currentFocused.z);

        if (!changed) {
            return;
        }

        Waypoint wpResult = WaypointManager.updateWaypoint(this.currentFocused, newWp);
        if (wpResult == null) {
            return;
        }

        this.reloadScreen();
    }

    private void updateFocusedWaypoint() {
        nameBox.setValue(this.currentFocused.name);
        xbox.setValue(String.valueOf(this.currentFocused.x));
        ybox.setValue(String.valueOf(this.currentFocused.y));
        zbox.setValue(String.valueOf(this.currentFocused.z));
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private String getWaypointBrief(Waypoint wp) {
        return wp.name + ": " + (int) wp.x + ", " + (int) wp.y + ", " + (int) wp.z;
    }

    public class Utils {
        public static CycleButton<String> makeDimensionWidget() {
            return makeDimensionWidget(100, 15);
        }

        public static CycleButton<String> makeDimensionWidget(int width, int height) {
            CycleButton<String> bt = CycleButton.builder((obj) -> {
                return Component.nullToEmpty(obj);
            }, "minecraft.overworld")
            .withValues("minecraft.overworld", "minecraft.the_nether", "minecraft.the_end")
            .displayOnlyValue()
            .create(0, 0, width, height, Component.empty());

            return bt;
        }
    }

    public class AddWaypointScreen extends Screen {
        private Screen parent;
        private Minecraft client;
        public AddWaypointScreen(Screen parent) {
            super(Component.empty());
            this.parent = parent;
            this.client = Minecraft.getInstance();
        }

        EditBox name;
        EditBox xbox, ybox, zbox;
        CycleButton<String> dimension;
        public void init() {
            LinearLayout layout = LinearLayout.vertical().spacing(4);
            layout.defaultCellSetting().alignHorizontallyCenter();

            name = layout.addChild(new EditBox(client.font, 0, 0, 200, 20, Component.empty()));

            LinearLayout coords = LinearLayout.horizontal().spacing(4);
            coords.defaultCellSetting().alignHorizontallyCenter();

            xbox = coords.addChild(new EditBox(client.font, 60, 20, Component.empty()));
            ybox = coords.addChild(new EditBox(client.font, 60, 20, Component.empty()));
            zbox = coords.addChild(new EditBox(client.font, 60, 20, Component.empty()));

            coords.arrangeElements();
            layout.addChild(coords);

            dimension = Utils.makeDimensionWidget(200, 20);
            layout.addChild(dimension);

            LinearLayout buttonsLayer = LinearLayout.horizontal().spacing(4);
            buttonsLayer.addChild(Button.builder(Component.nullToEmpty("Done"), (btn) -> {
                this.addWaypoint();                
            }).size(65, 20).build());

            buttonsLayer.addChild(Button.builder(Component.nullToEmpty("Set Defaults"), (btn) -> {
                this.autoFill();
            }).size(65, 20).build());

            buttonsLayer.addChild(Button.builder(Component.nullToEmpty("Cancel"), (btn) -> {
                this.exit();
            }).size(65, 20).build());

            layout.addChild(buttonsLayer);

            layout.arrangeElements();
            FrameLayout.centerInRectangle(layout, 0, 0, this.width, this.height);
            layout.visitWidgets( (widget) -> {
                this.addRenderableWidget(widget);
            } );

            this.autoFill();
        }

        private void exit() {
            this.clearWidgets();
            client.setScreen(this.parent);
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

            this.exit();
        }

        public void copy(Waypoint wp) {
            this.setCoords(wp.asVec3());
            this.dimension.setValue(wp.dimension);
            this.name.setValue("Copy of - " + wp.name);
        }

        private void autoFill() {
            Vec3 pos = client.player.getPosition(1.0f);
            this.setCoords(pos);

            this.dimension.setValue(WaypointManager.getCurrentDimensionString());
        }

        private void setCoords(Vec3 pos) {
            this.xbox.setValue(String.valueOf(pos.x));
            this.ybox.setValue(String.valueOf(pos.y));
            this.zbox.setValue(String.valueOf(pos.z));
        }

        @Override
        public void onClose() {
            client.setScreen(this.parent);
        }
    }

    public class WaypointButton extends AbstractButton {
        Minecraft client;
        public interface WButtonOnPress {
            void OnPress(Waypoint wp);
        }

        private WButtonOnPress onPress;
        private Waypoint wp;
        public WaypointButton(int x, int y, int width, int height, Component c, Waypoint wp, WButtonOnPress onPress) {
            super(x, y, width, height, c);
            this.wp = wp;
            this.onPress = onPress;
            this.client = minecraft.getInstance();
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
           this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.onPress.OnPress(this.wp);
        }

        @Override
        public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
            context.fill(this.getX(), this.getY(), this.width + this.getX(), this.height + this.getY(), 0xFFFFFFFF);
            //context.drawString(client.font, "T", this.getX(), this.getY() - (client.font.lineHeight / this.width), 0xFF000000, false);
        }
    }
}
