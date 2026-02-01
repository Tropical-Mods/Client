package tropical.client.features.jei;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class Jei {
    public void temp(ItemStack stack) {
        for (var entry : BuiltInRegistries.RECIPE_SERIALIZER.entrySet()) {
            RecipeSerializer<?> serializer = entry.getValue();
        }
    }
}
