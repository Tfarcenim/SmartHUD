package net.sleeplessdev.smarthud.render;

import com.mojang.blaze3d.platform.GlStateManager;
import lombok.NonNull;
import lombok.experimental.var;
import lombok.val;
import net.minecraft.client.gui.AbstractGui;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.data.HotbarStyle;
import net.sleeplessdev.smarthud.event.InventoryCache;
import net.sleeplessdev.smarthud.util.HandHelper;
import net.sleeplessdev.smarthud.util.IRenderEvent;
import net.sleeplessdev.smarthud.util.RenderContext;
import net.sleeplessdev.smarthud.util.StringHelper;

import static net.sleeplessdev.smarthud.config.ModulesConfig.HOTBAR_HUD;

public final class HotbarRender implements IRenderEvent {
    private static final ResourceLocation HUD_ELEMENTS = new ResourceLocation(SmartHUD.ID, "textures/hud/elements.png");

    @Override
    public boolean canRender() {
        return HOTBAR_HUD.isEnabled;
    }

    @Override
    public RenderGameOverlayEvent.ElementType getType() {
        return RenderGameOverlayEvent.ElementType.HOTBAR;
    }

    @Override
    public void onRenderTickPre(@NonNull final RenderContext ctx) {
        val cachedItems = InventoryCache.getInventory();
        val slots = cachedItems.size() >= HOTBAR_HUD.slotLimit
                    ? HOTBAR_HUD.slotLimit
                    : cachedItems.size();
        val center = ctx.getScreenWidth() / 2;
        val baseOffset = 98;

        if (cachedItems.size() > 0) {
            if (HOTBAR_HUD.hudStyle != HotbarStyle.INVISIBLE) {
                val width = 44 + (20 * (cachedItems.size() - 2)) - 2;
                val offset = (int) HandHelper.getSideOffset(baseOffset, width);

                renderHotbarBackground(ctx, center + offset, ctx.getScreenHeight() - 22, slots);
            }

            for (var i = 0; i < slots; i++) {
                val cachedItem = cachedItems.get(i);
                val stack = cachedItem.getStack();
                val stackOffset = baseOffset + 3 + (20 * i);
                val stackX = center + (int) HandHelper.getSideOffset(stackOffset, 16.0F);
                val stackY = ctx.getScreenHeight() - (16 + 3);

                val renderOverlay = !stack.isStackable() && HOTBAR_HUD.renderOverlays;
                val showStackSize = cachedItem.getCount() > 1 && HOTBAR_HUD.showStackSize;

                RenderHelper.enableGUIStandardItemLighting();
                ctx.renderItem(stack, stackX, stackY, true);

                if (renderOverlay) {
                    ctx.renderItemOverlays(stack, stackX, stackY);
                }

                RenderHelper.disableStandardItemLighting();

                if (showStackSize) {
                    val count = cachedItem.isMergeDuplicates()
                                ? cachedItem.getCount()
                                : cachedItem.getActualCount();
                    val stringWidth = ctx.getStringWidth(Integer.toString(count));
                    val labelOffset = baseOffset + (20 - stringWidth) + (20 * i);

                    var labelX = (int) (center + HandHelper.getSideOffset(labelOffset, stringWidth));
                    var labelY = ctx.getScreenHeight() - ctx.getFontHeight() - 1;

                    if (labelX < center) labelX += 18 - stringWidth;
                    // Keeps string to right edge of slot in left-handed mode

                    GlStateManager.disableDepth();
                    ctx.drawString(StringHelper.getAbbreviatedValue(count), labelX, labelY);
                }
            }
        } else if (HOTBAR_HUD.alwaysShow && HOTBAR_HUD.hudStyle != HotbarStyle.INVISIBLE) {
            val offset = (int) HandHelper.getSideOffset(baseOffset, 20.0F);

            renderHotbarBackground(ctx, center + offset, ctx.getScreenHeight() - 22, 1);
        }

        if (ctx.getGameSettings().attackIndicator == 2) {
            ctx.getGameSettings().attackIndicator = Integer.MIN_VALUE;
        }
    }

    @Override
    public void onRenderTickPost(@NonNull final RenderContext ctx) {
        if (ctx.getGameSettings().attackIndicator == Integer.MIN_VALUE) {
            if (ctx.getRenderViewEntity() instanceof PlayerEntity) {
                val player = (PlayerEntity) ctx.getRenderViewEntity();
                val side = player.getPrimaryHand().opposite();
                val strength = ctx.getPlayer().getCooledAttackStrength(0);

                if (strength < 1) {
                    val halfWidth = ctx.getScreenWidth() / 2;
                    val y = ctx.getScreenHeight() - 20;
                    val offset = 91 + getAttackIndicatorOffset();
                    val x = halfWidth + (side == HandSide.RIGHT ? -offset - 22 : offset + 6);
                    val strPixel = (int) (strength * 19);

                    GlStateManager.color(1, 1, 1, 1);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(
                        SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
                        SourceFactor.ONE, DestFactor.ZERO
                    );

                    RenderHelper.enableGUIStandardItemLighting();

                    ctx.bindTexture(AbstractGui.ICONS);
                    ctx.drawTexturedModalRect(x, y, 0, 94, 18, 18);
                    ctx.drawTexturedModalRect(x, y + 18 - strPixel, 18, 112 - strPixel, 18, strPixel);

                    RenderHelper.disableStandardItemLighting();

                    GlStateManager.disableRescaleNormal();
                    GlStateManager.disableBlend();
                }
            }
            ctx.getGameSettings().attackIndicator = 2;
        }
    }

    private int getAttackIndicatorOffset() {
        val cachedItems = InventoryCache.getInventory();
        val slot = 20, padding = 9;

        if (cachedItems.size() > 0) {
            val slots = cachedItems.size() < HOTBAR_HUD.slotLimit
                        ? cachedItems.size()
                        : HOTBAR_HUD.slotLimit;
            return (slot * slots) + padding;
        } else if (HOTBAR_HUD.alwaysShow) {
            return slot + padding;
        } else return 0;
    }

    private void renderHotbarBackground(
        @NonNull final RenderContext ctx,
        final int x,
        final int y,
        final int slots
    ) {
        val textureY = HOTBAR_HUD.hudStyle.getTextureY();

        ctx.bindTexture(HotbarRender.HUD_ELEMENTS);
        ctx.drawTexturedModalRect(x, y, 0, textureY, 11, 22);

        for (var i = 0; i < ((slots - 1) * 2); ++i) {
            val textureX = i % 2 == 0 ? 32 : 22;
            ctx.drawTexturedModalRect(x + (11 + (10 * i)), y, textureX, textureY, 10, 22);
        }

        ctx.drawTexturedModalRect(x + (20 * slots) - 9, y, 11, textureY, 11, 22);
    }
}
