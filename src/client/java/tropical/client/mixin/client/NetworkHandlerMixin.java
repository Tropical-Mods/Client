package tropical.client.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeMap;
import tropical.client.TropicalEvents;
import tropical.client.features.VillagerManager;

@Mixin(ClientPacketListener.class)
public class NetworkHandlerMixin {
    @Inject(method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V", at = @At("TAIL"))
    public void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        TropicalEvents.OnGameJoinCallback.EVENT.invoker().interact(packet);
    }

    @Inject(method = "handleMerchantOffers", cancellable = true, at = @At("HEAD"))
    public void handleMerchantOffsers(ClientboundMerchantOffersPacket packet, CallbackInfo ci) {
        TropicalEvents.OnMerchantOffsersCallback.EVENT.invoker().interact(packet);
    }

    @Inject(method = "handleOpenScreen", cancellable = true, at=@At("HEAD"))
    public void handleOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!VillagerManager.isWindowOpen() && packet.getType() == MenuType.MERCHANT) {
            ci.cancel();
            ClientPlayNetworking.getSender().sendPacket(
                new ServerboundContainerClosePacket(packet.getContainerId())
            );
        }
    }
}
