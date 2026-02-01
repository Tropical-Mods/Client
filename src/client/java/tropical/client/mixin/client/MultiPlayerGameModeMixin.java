package tropical.client.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import tropical.client.features.VillagerManager;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "interact", at = @At("HEAD"))
    public void interact(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        if (!(entity instanceof Merchant)) { return; }
        if (VillagerManager.getLastId() == null) { return; }
        if (entity.getId() == VillagerManager.getLastId() && !VillagerManager.getOffers().isEmpty()) {
            VillagerManager.setOpenWindow(true);
        }
    }
}
