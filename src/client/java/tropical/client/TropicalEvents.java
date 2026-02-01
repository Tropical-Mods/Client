package tropical.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;

public class TropicalEvents {
    public interface OnGameJoinCallback {
        Event<OnGameJoinCallback> EVENT = EventFactory.createArrayBacked(OnGameJoinCallback.class,
            (listeners) -> (packet) -> {
                for(OnGameJoinCallback listener : listeners) {
                    listener.interact(packet);
                }
            }
        );

        void interact(ClientboundLoginPacket packet);
    }

    public interface OnMerchantOffsersCallback {
        Event<OnMerchantOffsersCallback> EVENT = EventFactory.createArrayBacked(OnMerchantOffsersCallback.class,
            (listeners) -> (packet) -> {
                for(OnMerchantOffsersCallback listener : listeners) {
                    listener.interact(packet);
                }
            }
        );

        void interact(ClientboundMerchantOffersPacket packet);
    }
}
