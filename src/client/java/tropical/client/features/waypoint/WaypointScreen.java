package tropical.client.features.waypoint;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.FocusableTextWidget.BackgroundFill;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class WaypointScreen extends Screen {
    public record WaypointListEntry(FocusableTextWidget widget, Waypoint wp, Vec2 dir) {}

    //private LinearLayout layout;
    private ArrayList<WaypointListEntry> entries;
    private Minecraft client;
    private HeaderAndFooterLayout lay;

    private int guiScale;

    private @Nullable Waypoint currentFocused = null;

    public Screen parent;
    public WaypointScreen(Screen parent) {
        super(Component.nullToEmpty("Waypoints"));
        this.parent = parent;
        this.client = Minecraft.getInstance();
        this.guiScale = client.getWindow().getGuiScale();
        //this.layout = LinearLayout.vertical().spacing(4);
    }

    @Override
    protected void init() {
        this.lay = new HeaderAndFooterLayout(this, 49, 60);
        this.load();
    }

    public void load() {
        LinearLayout headerLayout = this.lay.addToHeader(LinearLayout.vertical().spacing(4));
        headerLayout.defaultCellSetting().alignHorizontallyCenter();
        headerLayout.addChild(new StringWidget(this.title, this.font));

        headerLayout.arrangeElements();

        ScrollableLayout list = this.makeWaypointsList(this.lay.getContentHeight(),
                                lay.getFooterHeight(), lay.getHeaderHeight());

        GridLayout contentLayout = this.lay.addToContents(new GridLayout(1, 2).columnSpacing(7));
        contentLayout.defaultCellSetting().alignHorizontallyCenter();
        contentLayout.addChild(new WaypointMapWidget(0, 0,
            //this series of subtractions is fucking stupid, all of these values should be globals
            // 20 = 
            // 10 = margin for content border times two
            // 4 = the column spacing for the grid layout
            this.width-list.getWidth()-20-10-4, this.lay.getContentHeight(),
            Component.empty(),
            this.entries
        ), 1, 2);

        contentLayout.addChild(list, 1, 1);

        contentLayout.arrangeElements();

        //this.lay.addToContents(list);

        this.lay.arrangeElements();

        this.makeFooter();

        this.lay.visitWidgets( (widget) -> {
            this.addRenderableWidget(widget);
        } );
    }

    private void makeWaypointMap(int listWidth) {
    }

    private Vec2 getDir(Waypoint wp) {
        Vec3 pos = new Vec3(client.player.xOld, client.player.yOld, client.player.zOld);
        Vec3 wpVec = wp.asVec3();

        Vec3 dirToWp = pos.subtract(wpVec);
        Vec2 topDownToWp = new Vec2((float)dirToWp.x, (float)dirToWp.z);

        return topDownToWp;
    }

    private ScrollableLayout makeWaypointsList(int contentHeight, int footerHeight, int headerHeight) {
        LinearLayout layout = LinearLayout.vertical().spacing(4);
        int t = 0;
        this.entries = new ArrayList<>();
        var wps = WaypointManager.getWaypoints();
        for (int i = 0; i < wps.size(); i++) {
            LinearLayout row = LinearLayout.horizontal().spacing(4);
            Waypoint wp = wps.get(i);
            String line = this.getWaypointBrief(wp);
            Font font = Minecraft.getInstance().font;
            FocusableTextWidget w = FocusableTextWidget.builder(Component.nullToEmpty(line), font)
                                .backgroundFill(BackgroundFill.ALWAYS).build();

            //int x = 10;
            //int y = w.getHeight() * (i);
            //w.setX(x);
            //w.setY(y);
            w.setWidth(150);

            WaypointButton toggleButton = new WaypointButton(
                0, 0, 10, 10,
                Component.nullToEmpty("nothing"), wp,
                (waypoint) -> {
                    WaypointManager.toggleWaypoint(waypoint);
                }
            );

            row.addChild(w);
            row.addChild(toggleButton);

            row.arrangeElements();

            layout.addChild(row);

            Vec2 dir = getDir(wp);
            this.entries.add(new WaypointListEntry(w, wp, dir));
        }

        layout.arrangeElements();


        ScrollableLayout scroll = new ScrollableLayout(Minecraft.getInstance(), layout, 10);
        scroll.setMaxHeight(contentHeight);
        //scroll.setMinWidth(this.width - 20);
        scroll.setX(0);
        scroll.setY(headerHeight);
        scroll.arrangeElements();

        return scroll;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        boolean isFocused = (this.currentFocused != null);
        this.saveButton.active = isFocused;
        this.removeButton.active = isFocused;
        this.copyButton.active = isFocused;

        //drawBorder
        int headerY = this.lay.getHeaderHeight();
        int footerY = this.height - this.lay.getFooterHeight();
        int widthMargin = 5;
        int heightMargin = 7; 
        context.renderOutline(0+widthMargin, headerY-heightMargin, this.width-(widthMargin*2), (footerY-headerY)+(heightMargin*2), 0xFF888888);

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
        Minecraft.getInstance().setScreen(new WaypointScreen(null));
    }

    EditBox nameBox;
    EditBox xbox, zbox, ybox;
    Button removeButton, saveButton, copyButton;
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
        saveButton = buttonGrid.addChild(Button.builder(Component.nullToEmpty("Save Changes"), (btn) -> {
            this.updateWaypoint();
        }).size(100, 20).build(), 1, 1);
        
        removeButton = buttonGrid.addChild(Button.builder(Component.nullToEmpty("Delete"), (btn) -> {
            this.removeWaypoint();
        }).size(100, 20).build(), 1, 2);

        copyButton = buttonGrid.addChild(Button.builder(Component.nullToEmpty("Copy"), (btn) -> {
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

    public class WaypointMapWidget extends AbstractWidget {
        private ArrayList<WaypointListEntry> entries;
        private int radius;
        private int scale;

        private static int blocksRadius = 1000;

        private float[][] circleMesh = new float[360][2];

        public WaypointMapWidget(int i, int j, int k, int l, Component component, ArrayList<WaypointListEntry> wps) {
            super(i, j, k, l, component);
            this.entries = wps;

            this.radius = Math.min(this.height, this.width);
            this.scale = Minecraft.getInstance().getWindow().getGuiScale();

            this.loadCricleMesh();
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
           this.defaultButtonNarrationText(narrationElementOutput);
        }

        private void loadCricleMesh() {
            for (int i = 0; i < 360; i++) {
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
            context.hLine(0, length, 0, 0xFFFF00FF);

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
            context.hLine(0, length - 1, 0, 0xFFFF00FF);

            matrices.popMatrix();
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            for (WaypointListEntry entry : this.entries) {
                this.drawWaypointLine(context, entry.dir); 
            }

            this.drawCircle(context);
            //context.fill(this.getX(), this.getY(), this.width+this.getX(), this.height+this.getY(), 0xFFFF0000);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            return;
        }

        @Override
        public void onDrag(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
            WaypointMapWidget.blocksRadius += (deltaY * 2);
        }
    }
}
