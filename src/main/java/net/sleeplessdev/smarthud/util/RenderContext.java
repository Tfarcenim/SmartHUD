package net.sleeplessdev.smarthud.util;

import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;

@Getter
@OnlyIn(Dist.CLIENT)
public final class RenderContext {
    private final Minecraft client;
    private final int screenWidth;
    private final int screenHeight;
    private final float partialTicks;

    public RenderContext(@NonNull final Minecraft mc, @NonNull final RenderGameOverlayEvent event) {
        client = mc;
        screenWidth = event.getResolution().getScaledWidth();
        screenHeight = event.getResolution().getScaledHeight();
        partialTicks = event.getPartialTicks();
    }

    public GameSettings getGameSettings() {
        return client.gameSettings;
    }

    public PlayerEntity getPlayer() {
        return client.player;
    }

    public Entity getRenderViewEntity() {
        return client.getRenderViewEntity();
    }

    public int getStringWidth(@NonNull final String text) {
        return client.fontRenderer.getStringWidth(text);
    }

    public int getFontHeight() {
        return client.fontRenderer.FONT_HEIGHT;
    }

    public void bindTexture(@NonNull final ResourceLocation texture) {
        client.getTextureManager().bindTexture(texture);
    }

    public void drawTexturedModalRect(final int x, final int y, final int textureX, final int textureY, final int width, final int height) {
        client.ingameGUI.drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    public void drawString(@NonNull final String text, final float x, final float y, final int color) {
        client.fontRenderer.drawString(text, x, y, color, true);
    }

    public void drawString(@NonNull final String text, final float x, final float y) {
        client.fontRenderer.drawString(text, x, y, 0xFFFFFFFF, true);
    }

    public void renderItem(@NonNull final ItemStack stack, final int x, final int y, final boolean includeEffect) {
        if (includeEffect) {
            client.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        } else client.getRenderItem().renderItemIntoGUI(stack, x, y);
    }

    public void renderItemOverlays(@NonNull final ItemStack stack, final int x, final int y) {
        client.getRenderItem().renderItemOverlays(client.fontRenderer, stack, x, y);
    }
}
