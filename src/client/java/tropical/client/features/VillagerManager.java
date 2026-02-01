package tropical.client.features;

import java.util.ArrayList;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerRegistries.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import tropical.client.TropicalClient;
import tropical.client.TropicalEvents;

public class VillagerManager {
    @Nullable
    private static Integer lastId = null;
    private static ArrayList<MerchantOffer> offers = new ArrayList<>();
    private static boolean openWindow = false;

    public static void initalize() {
        TropicalEvents.OnMerchantOffsersCallback.EVENT.register( (packet) -> {
            VillagerManager.setOffers(packet.getOffers());
        });

        HudElementRegistry.attachElementAfter(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(TropicalClient.MOD_ID, "villager_overlay"),
            VillagerManager::render 
        );
    }

    public static void render(GuiGraphics context, DeltaTracker tickCounter) {
        var font = Minecraft.getInstance().font;
        if (lastId == null) { return; }

        int i = 0;
        for (MerchantOffer offer : offers) {
            int xOffset = 0;
            int yOffset = 0 + (i*20);

            ItemStack firstBuy = offer.getCostA().copy();
            ItemStack secondBuy = offer.getCostB().copy();
            ItemStack sell = offer.getResult().copy();

            context.renderItem(firstBuy, xOffset, yOffset);
            context.renderItemDecorations(font, firstBuy, xOffset, yOffset);

            xOffset += 20;
            context.renderItem(secondBuy, xOffset, yOffset);
            context.renderItemDecorations(font, secondBuy, xOffset, yOffset);

            xOffset += 20;
            if (!offer.isOutOfStock()) {
                context.drawString(font, "-->", xOffset, yOffset + (font.lineHeight/2), 0xFFFFFFFF, true);
            } else {
                context.drawString(font, " X ", xOffset, yOffset + (font.lineHeight/2), 0xFFFFFFFF, true);
            }

            xOffset += 20;
            context.renderItem(sell, xOffset, yOffset);
            context.renderItemDecorations(font, sell, xOffset, yOffset);

            xOffset += 20;
            String enchants = getEnchantmentText(offer);
            context.drawString(font, enchants, xOffset, yOffset + (font.lineHeight/2), 0xFFFFFFFF, true);

            i++;
        }
    }

    private static String getEnchantmentText(MerchantOffer offer) {
        ArrayList<String> enchants = new ArrayList<>();

        var component = EnchantmentHelper.getEnchantmentsForCrafting(offer.getResult());
        if (EnchantmentHelper.hasAnyEnchantments(offer.getResult())) {
            for (var entry : component.entrySet()) {
                var level = entry.getIntValue();
                enchants.add(Enchantment.getFullname(entry.getKey(), level).getString());
            }
        }

        return String.join(", ", enchants);
    }

    public static void setOffers(ArrayList<MerchantOffer> o) {
        offers = o;
    }

    public static boolean isWindowOpen() {
        return openWindow;
    }

    public static void setOpenWindow(boolean newValue) {
        openWindow = newValue;
    }

    public static Integer getLastId() {
        return lastId;
    }

    public static ArrayList<MerchantOffer> getOffers() {
        return offers;
    }

    public static void updateLookingAt(HitResult hitResult) {
        var client = Minecraft.getInstance();
        if (Objects.isNull(hitResult) || hitResult.getType() != Type.ENTITY) {
            lastId = null;
            return;
        }

        EntityHitResult entityHit = (EntityHitResult)hitResult;
        Entity entity = entityHit.getEntity();
        if (!(entity instanceof Merchant)) {
            return;
        }

        if (!(entity instanceof Villager)) {
            lastId = null;
            return;
        }

        var profession = ((Villager)entity).getVillagerData().profession();
        if (profession.is(VillagerProfession.NONE) || profession.is(VillagerProfession.NITWIT)) {
            return;
        }

        if (lastId != null && (int)lastId == entity.getId()) {
            return;
        }

        offers = new ArrayList<>();
        lastId = entity.getId();

        ClientPlayNetworking.getSender().sendPacket(
            ServerboundInteractPacket.createInteractionPacket(
                entity,
                client.player.isShiftKeyDown(),
                InteractionHand.MAIN_HAND
            )
        );
    }
}
