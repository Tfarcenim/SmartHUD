package net.sleeplessdev.smarthud.util;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public final class RenderContext {
    private final Minecraft client;
    public final int screenWidth;
    public final int screenHeight;
    public final float partialTicks;

    public RenderContext(final Minecraft mc, final RenderGameOverlayEvent event) {
        client = mc;
        screenWidth = mc.getMainWindow().getScaledWidth();
        screenHeight = mc.getMainWindow().getScaledHeight();
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

    public int getStringWidth(final String text) {
        return client.fontRenderer.getStringWidth(text);
    }

    public int getFontHeight() {
        return client.fontRenderer.FONT_HEIGHT;
    }

    public void bindTexture(final ResourceLocation texture) {
        client.getTextureManager().bindTexture(texture);
    }

    public void drawTexturedModalRect(final int x, final int y, final int textureX, final int textureY, final int width, final int height) {
        client.ingameGUI.blit(x, y, textureX, textureY, width, height);
    }

    public void drawStringWithShadow(final String text, final float x, final float y, final int color) {
        client.fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    public void drawStringWithShadow(final String text, final float x, final float y) {
        client.fontRenderer.drawStringWithShadow(text, x, y, 0xFFFFFFFF);
    }

    public void renderItem(final ItemStack stack, final int x, final int y, final boolean includeEffect) {
        if (includeEffect) {
            client.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);
        } else client.getItemRenderer().renderItemIntoGUI(stack, x, y);
    }

    public void renderItemOverlays(final ItemStack stack, final int x, final int y) {
        client.getItemRenderer().renderItemOverlays(client.fontRenderer, stack, x, y);
    }
}
