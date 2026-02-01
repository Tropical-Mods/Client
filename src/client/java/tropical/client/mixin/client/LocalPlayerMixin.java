package tropical.client.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.LocalPlayer;
import tropical.client.features.VillagerManager;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "closeContainer", at=@At("HEAD"))
    public void closeContainer(CallbackInfo ci) {
        VillagerManager.setOpenWindow(false);
    }
}
