package net.sleeplessdev.smarthud.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.config.ModulesConfig;
import net.sleeplessdev.smarthud.data.HotbarStyle;
import net.sleeplessdev.smarthud.event.InventoryCache;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.HandHelper;
import net.sleeplessdev.smarthud.util.IRenderEvent;
import net.sleeplessdev.smarthud.util.RenderContext;
import net.sleeplessdev.smarthud.util.StringHelper;

public final class HotbarRender implements IRenderEvent {
    private static final ResourceLocation HUD_ELEMENTS = new ResourceLocation(SmartHUD.ID, "textures/hud/elements.png");

    public HotbarRender() {}

    @Override
    public boolean canRender() {
        return ModulesConfig.HOTBAR_HUD.isEnabled;
    }

    @Override
    public RenderGameOverlayEvent.ElementType getType() {
        return RenderGameOverlayEvent.ElementType.HOTBAR;
    }

    @Override
    public void onRenderTickPre(RenderContext ctx) {
        final ImmutableList<CachedItem> cachedItems = InventoryCache.getInventory();
        final int slots = cachedItems.size() >= ModulesConfig.HOTBAR_HUD.slotLimit
                          ? ModulesConfig.HOTBAR_HUD.slotLimit
                          : cachedItems.size();
        final int center = ctx.getScreenWidth() / 2;
        final int baseOffset = 98;

        if (cachedItems.size() > 0) {
            if (ModulesConfig.HOTBAR_HUD.hudStyle != HotbarStyle.INVISIBLE) {
                final int width = 44 + (20 * (cachedItems.size() - 2)) - 2;
                final int offset = (int) HandHelper.handleVariableOffset(baseOffset, width);

                renderHotbarBackground(ctx, center + offset, ctx.getScreenHeight() - 22, slots);
            }

            for (int i = 0; i < slots; i++) {
                final CachedItem cachedItem = cachedItems.get(i);
                final ItemStack stack = cachedItem.getStack();
                final int stackOffset = baseOffset + 3 + (20 * i);
                final int stackX = center + (int) HandHelper.handleVariableOffset(stackOffset, 16.0F);
                final int stackY = ctx.getScreenHeight() - (16 + 3);

                final boolean renderOverlay = !stack.isStackable() && ModulesConfig.HOTBAR_HUD.renderOverlays;
                final boolean showStackSize = cachedItem.getCount() > 1 && ModulesConfig.HOTBAR_HUD.showStackSize;

                RenderHelper.enableGUIStandardItemLighting();
                ctx.renderItem(stack, stackX, stackY, true);

                if (renderOverlay) {
                    ctx.renderItemOverlays(stack, stackX, stackY);
                }

                RenderHelper.disableStandardItemLighting();

                if (showStackSize) {
                    final int count = cachedItem.isMergeDuplicates()
                                      ? cachedItem.getCount()
                                      : cachedItem.getActualCount();
                    final int stringWidth = ctx.getStringWidth(Integer.toString(count));
                    final int labelOffset = baseOffset + (20 - stringWidth) + (20 * i);

                    int labelX = (int) (center + HandHelper.handleVariableOffset(labelOffset, stringWidth));
                    int labelY = ctx.getScreenHeight() - ctx.getFontHeight() - 1;

                    if (labelX < center) labelX += 18 - stringWidth;
                    // Keeps string to right edge of slot in left-handed mode

                    GlStateManager.disableDepth();
                    ctx.drawString(StringHelper.getAbbreviatedValue(count), labelX, labelY);
                }
            }
        } else if (ModulesConfig.HOTBAR_HUD.alwaysShow && ModulesConfig.HOTBAR_HUD.hudStyle != HotbarStyle.INVISIBLE) {
            final int offset = (int) HandHelper.handleVariableOffset(baseOffset, 20.0F);

            renderHotbarBackground(ctx, center + offset, ctx.getScreenHeight() - 22, 1);
        }

        if (ctx.getGameSettings().attackIndicator == 2) {
            ctx.getGameSettings().attackIndicator = Integer.MIN_VALUE;
        }
    }

    @Override
    public void onRenderTickPost(RenderContext ctx) {
        if (ctx.getGameSettings().attackIndicator == Integer.MIN_VALUE) {
            if (ctx.getRenderViewEntity() instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer) ctx.getRenderViewEntity();
                final EnumHandSide side = player.getPrimaryHand().opposite();
                final float strength = ctx.getPlayer().getCooledAttackStrength(0);

                if (strength < 1) {
                    final int halfWidth = ctx.getScreenWidth() / 2;
                    final int y = ctx.getScreenHeight() - 20;
                    final int offset = 91 + getAttackIndicatorOffset();
                    final int x = halfWidth + (side == EnumHandSide.RIGHT ? -offset - 22 : offset + 6);
                    final int strPixel = (int) (strength * 19);

                    GlStateManager.color(1, 1, 1, 1);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(
                        SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
                        SourceFactor.ONE, DestFactor.ZERO
                    );

                    RenderHelper.enableGUIStandardItemLighting();

                    ctx.bindTexture(Gui.ICONS);
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
        final ImmutableList<CachedItem> cachedItems = InventoryCache.getInventory();
        final int slot = 20, padding = 9;

        if (cachedItems.size() > 0) {
            final int slots = cachedItems.size() < ModulesConfig.HOTBAR_HUD.slotLimit
                              ? cachedItems.size()
                              : ModulesConfig.HOTBAR_HUD.slotLimit;

            return (slot * slots) + padding;
        } else if (ModulesConfig.HOTBAR_HUD.alwaysShow) {
            return slot + padding;
        } else return 0;
    }

    private void renderHotbarBackground(RenderContext ctx, int x, int y, int slots) {
        final int textureY = ModulesConfig.HOTBAR_HUD.hudStyle.getTextureY();

        ctx.bindTexture(HotbarRender.HUD_ELEMENTS);
        ctx.drawTexturedModalRect(x, y, 0, textureY, 11, 22);

        for (int i = 0; i < ((slots - 1) * 2); ++i) {
            final int textureX = i % 2 == 0 ? 32 : 22;
            ctx.drawTexturedModalRect(x + (11 + (10 * i)), y, textureX, textureY, 10, 22);
        }

        ctx.drawTexturedModalRect(x + (20 * slots) - 9, y, 11, textureY, 11, 22);
    }
}
