package net.sleeplessdev.smarthud.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.AbstractGui;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.data.HotbarStyle;
import net.sleeplessdev.smarthud.event.InventoryCache;
import net.sleeplessdev.smarthud.util.*;

import java.util.List;

import static net.sleeplessdev.smarthud.config.ModulesConfig.HOTBAR_HUD;

public final class HotbarRender implements RenderEvent {
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
    public void onRenderTickPre(final RenderContext ctx) {
        List<CachedItem> cachedItems = InventoryCache.getInventory();
        int slots = cachedItems.size() >= HOTBAR_HUD.slotLimit
                    ? HOTBAR_HUD.slotLimit
                    : cachedItems.size();
        int center = ctx.screenWidth / 2;
        int baseOffset = 98;

        if (cachedItems.size() > 0) {
            if (HOTBAR_HUD.hudStyle != HotbarStyle.INVISIBLE) {
                int width = 44 + (20 * (cachedItems.size() - 2)) - 2;
                int offset = (int) HandHelper.getSideOffset(baseOffset, width);

                renderHotbarBackground(ctx, center + offset, ctx.screenHeight - 22, slots);
            }

            for (int i = 0; i < slots; i++) {
                CachedItem cachedItem = cachedItems.get(i);
                ItemStack stack = cachedItem.stack;
                int stackOffset = baseOffset + 3 + (20 * i);
                int stackX = center + (int) HandHelper.getSideOffset(stackOffset, 16.0F);
                int stackY = ctx.screenHeight - 19;

                boolean renderOverlay = !stack.isStackable() && HOTBAR_HUD.renderOverlays;
                boolean showStackSize = cachedItem.count > 1 && HOTBAR_HUD.showStackSize;

                RenderHelper.enableGUIStandardItemLighting();
                ctx.renderItem(stack, stackX, stackY, true);

                if (renderOverlay) {
                    ctx.renderItemOverlays(stack, stackX, stackY);
                }

                RenderHelper.disableStandardItemLighting();

                if (showStackSize) {
                    int count = cachedItem.isMergeDuplicates()
                                ? cachedItem.count
                                : cachedItem.actualCount;
                    int stringWidth = ctx.getStringWidth(Integer.toString(count));
                    int labelOffset = baseOffset + (20 - stringWidth) + (20 * i);

                    int labelX = (int) (center + HandHelper.getSideOffset(labelOffset, stringWidth));
                    int labelY = ctx.screenHeight - ctx.getFontHeight() - 1;

                    if (labelX < center) labelX += 18 - stringWidth;
                    // Keeps string to right edge of slot in left-handed mode

                    GlStateManager.disableDepthTest();
                    ctx.drawStringWithShadow(StringHelper.getAbbreviatedValue(count), labelX, labelY);
                }
            }
        } else if (HOTBAR_HUD.alwaysShow && HOTBAR_HUD.hudStyle != HotbarStyle.INVISIBLE) {
            int offset = (int) HandHelper.getSideOffset(baseOffset, 20.0F);

            renderHotbarBackground(ctx, center + offset, ctx.screenHeight - 22, 1);
        }

        if (ctx.getGameSettings().attackIndicator == AttackIndicatorStatus.HOTBAR) {
            ctx.getGameSettings().attackIndicator = AttackIndicatorStatus.OFF;//todo
        }
    }

    @Override
    public void onRenderTickPost(final RenderContext ctx) {
        if (ctx.getGameSettings().attackIndicator == AttackIndicatorStatus.OFF) {//todo
            if (ctx.getRenderViewEntity() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) ctx.getRenderViewEntity();
                HandSide side = player.getPrimaryHand().opposite();
                double strength = ctx.getPlayer().getCooledAttackStrength(0);

                if (strength < 1) {
                    int halfWidth = ctx.screenWidth / 2;
                    int y = ctx.screenHeight - 20;
                    int offset = 91 + getAttackIndicatorOffset();
                    int x = halfWidth + (side == HandSide.RIGHT ? -offset - 22 : offset + 6);
                    int strPixel = (int) (strength * 19);

                    GlStateManager.color4f(1, 1, 1, 1);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFuncSeparate(
                        SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
                        SourceFactor.ONE, DestFactor.ZERO
                    );

                    RenderHelper.enableGUIStandardItemLighting();

                    ctx.bindTexture(AbstractGui.GUI_ICONS_LOCATION);
                    ctx.drawTexturedModalRect(x, y, 0, 94, 18, 18);
                    ctx.drawTexturedModalRect(x, y + 18 - strPixel, 18, 112 - strPixel, 18, strPixel);

                    RenderHelper.disableStandardItemLighting();

                    GlStateManager.disableRescaleNormal();
                    GlStateManager.disableBlend();
                }
            }
            ctx.getGameSettings().attackIndicator = AttackIndicatorStatus.CROSSHAIR;
        }
    }

    private int getAttackIndicatorOffset() {
        List<CachedItem> cachedItems = InventoryCache.getInventory();
        int slot = 20, padding = 9;

        if (cachedItems.size() > 0) {
            int slots = cachedItems.size() < HOTBAR_HUD.slotLimit
                        ? cachedItems.size()
                        : HOTBAR_HUD.slotLimit;
            return (slot * slots) + padding;
        } else if (HOTBAR_HUD.alwaysShow) {
            return slot + padding;
        } else return 0;
    }

    private void renderHotbarBackground(
        final RenderContext ctx,
        final int x,
        final int y,
        final int slots
    ) {
        int textureY = HOTBAR_HUD.hudStyle.textureY;

        ctx.bindTexture(HotbarRender.HUD_ELEMENTS);
        ctx.drawTexturedModalRect(x, y, 0, textureY, 11, 22);

        for (int i = 0; i < ((slots - 1) * 2); ++i) {
            int textureX = i % 2 == 0 ? 32 : 22;
            ctx.drawTexturedModalRect(x + (11 + (10 * i)), y, textureX, textureY, 10, 22);
        }

        ctx.drawTexturedModalRect(x + (20 * slots) - 9, y, 11, textureY, 11, 22);
    }
}
