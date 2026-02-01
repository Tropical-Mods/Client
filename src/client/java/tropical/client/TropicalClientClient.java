package tropical.client;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import tropical.client.features.VillagerManager;
import tropical.client.features.waypoint.WaypointManager;
import tropical.client.features.waypoint.WaypointRender;

public class TropicalClientClient implements ClientModInitializer {
    KeyMapping waypointBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.tropical.client.waypoint",
        GLFW.GLFW_KEY_J, 
        KeyMapping.Category.MISC
    ));

	@Override
	public void onInitializeClient() {
        WaypointManager.initalize();
        WaypointRender.initalize();

        VillagerManager.initalize();

        ClientTickEvents.END_WORLD_TICK.register((world) -> {
            Minecraft client = Minecraft.getInstance();
            if (client == null) { return; }
            if (client.screen != null) { return; }
            VillagerManager.updateLookingAt(Minecraft.getInstance().hitResult);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (waypointBind.consumeClick()) {
                WaypointManager.toggleScreen();
            }
        });

        ClientTickEvents.END_WORLD_TICK.register( levelClient -> {
            TropicalUtils.currentDimension = TropicalUtils.getCurrentDimension();
        });
	}
}
