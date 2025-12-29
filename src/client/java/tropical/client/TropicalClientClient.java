package tropical.client;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.world.level.Level;
import tropical.client.features.waypoint.WaypointManager;
import tropical.client.features.waypoint.WaypointRender;

public class TropicalClientClient implements ClientModInitializer {
    KeyMapping waypointBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.tropical.client.waypoint",
        GLFW.GLFW_KEY_F13, 
        KeyMapping.Category.MISC
    ));

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        WaypointManager.initalize();
        WaypointRender.initalize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (waypointBind.consumeClick()) {
                WaypointManager.toggleScreen();
                WaypointManager.dimensionKeyToString();
            }
        });
	}
}
