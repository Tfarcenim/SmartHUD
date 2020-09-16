package net.sleeplessdev.smarthud.render;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.sleeplessdev.smarthud.config.ModulesConfig;
import net.sleeplessdev.smarthud.data.HotbarStyle;
import net.sleeplessdev.smarthud.util.*;

import java.util.List;


public final class HotbarRender implements RenderEvent {
    private static final ResourceLocation HUD_ELEMENTS = new ResourceLocation(SmartHUD.ID, "textures/hud/elements.png");

    @Override
    public boolean canRender() {
        return ModulesConfig.enableHotbar.get();
    }

    @Override
    public RenderGameOverlayEvent.ElementType getType() {
        return RenderGameOverlayEvent.ElementType.HOTBAR;
    }

    @Override
    public void onRenderTickPre(final RenderContext ctx) {
        List<CachedItem> cachedItems = SmartHUD.inventory;
        int slots = cachedItems.size() >= ModulesConfig.slotLimit.get()
                    ? ModulesConfig.slotLimit.get()
                    : cachedItems.size();
        int center = ctx.screenWidth / 2;
        int baseOffset = 98;

        int yOffset = ctx.screenHeight + ModulesConfig.yOffset.get();

        if (cachedItems.size() > 0) {
            if (ModulesConfig.hotbarStyle != HotbarStyle.INVISIBLE) {
                int width =  20 * cachedItems.size() + 6;
                int xOffset = (int) HandHelper.getSideOffset(baseOffset, width);

                renderHotbarBackground(ctx, center + xOffset, yOffset, slots);
            }

            for (int i = 0; i < slots; i++) {
                CachedItem cachedItem = cachedItems.get(i);
                ItemStack stack = cachedItem.stack;
                int stackOffset = baseOffset + 3 + (20 * i);
                int stackX = center + (int) HandHelper.getSideOffset(stackOffset, 16.0F);
                int stackY = yOffset + 3;

                boolean renderOverlay = !stack.isStackable() && ModulesConfig.renderOverlays.get();
                boolean showStackSize = cachedItem.count > 1 && ModulesConfig.showStackSize.get();

                RenderHelper.enableStandardItemLighting();
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

                    RenderSystem.disableDepthTest();
                    ctx.drawStringWithShadow(ctx.matrices,StringHelper.getAbbreviatedValue(count), labelX, labelY);
                }
            }
        } else if (ModulesConfig.alwaysShow.get() && ModulesConfig.hotbarStyle != HotbarStyle.INVISIBLE) {
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

                    RenderSystem.color4f(1, 1, 1, 1);
                    RenderSystem.enableRescaleNormal();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(
                        SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
                        SourceFactor.ONE, DestFactor.ZERO
                    );

                    RenderHelper.enableStandardItemLighting();

                    ctx.bindTexture(AbstractGui.GUI_ICONS_LOCATION);
                    ctx.drawTexturedModalRect(ctx.matrices,x, y, 0, 94, 18, 18);
                    ctx.drawTexturedModalRect(ctx.matrices,x, y + 18 - strPixel, 18, 112 - strPixel, 18, strPixel);

                    RenderHelper.disableStandardItemLighting();

                    RenderSystem.disableRescaleNormal();
                    RenderSystem.disableBlend();
                }
            }
            ctx.getGameSettings().attackIndicator = AttackIndicatorStatus.CROSSHAIR;
        }
    }

    private int getAttackIndicatorOffset() {
        List<CachedItem> cachedItems = SmartHUD.inventory;
        int slot = 20, padding = 9;

        if (cachedItems.size() > 0) {
            int slots = cachedItems.size() < ModulesConfig.slotLimit.get()
                        ? cachedItems.size()
                        : ModulesConfig.slotLimit.get();
            return (slot * slots) + padding;
        } else if (ModulesConfig.alwaysShow.get()) {
            return slot + padding;
        } else return 0;
    }

    private void renderHotbarBackground(
        final RenderContext ctx,
        final int x,
        final int y,
        final int slots
    ) {
        int textureY = ModulesConfig.hotbarStyle.textureY;

        ctx.bindTexture(HotbarRender.HUD_ELEMENTS);
        ctx.drawTexturedModalRect(ctx.matrices,x, y, 0, textureY, 11, 22);

        for (int i = 0; i < ((slots - 1) * 2); ++i) {
            int textureX = i % 2 == 0 ? 32 : 22;
            ctx.drawTexturedModalRect(ctx.matrices,x + (11 + (10 * i)), y, textureX, textureY, 10, 22);
        }

        ctx.drawTexturedModalRect(ctx.matrices,x + (20 * slots) - 9, y, 11, textureY, 11, 22);
    }
}
