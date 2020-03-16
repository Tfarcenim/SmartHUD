package net.sleeplessdev.smarthud.render;

import com.google.common.collect.EvictingQueue;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.NonNull;
import lombok.experimental.var;
import lombok.val;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.config.ModulesConfig;
import net.sleeplessdev.smarthud.event.ItemPickupQueue;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.HandHelper;
import net.sleeplessdev.smarthud.util.IRenderEvent;
import net.sleeplessdev.smarthud.util.RenderContext;
import net.sleeplessdev.smarthud.util.StringHelper;
import net.sleeplessdev.smarthud.util.interpolation.CubicBezierInterpolator;
import net.sleeplessdev.smarthud.util.interpolation.Interpolator;

public final class ItemPickupRender implements IRenderEvent {
    private static final Interpolator ANIMATION = new CubicBezierInterpolator(0.42D, 0.0D, 0.58D, 1.0D);
    private static final float ANIMATION_DURATION = 10.0F;

    @Override
    public boolean canRender() {
        return ModulesConfig.ITEM_PICKUP_HUD.isEnabled;
    }

    @Override
    public RenderGameOverlayEvent.ElementType getType() {
        return RenderGameOverlayEvent.ElementType.TEXT;
    }

    @Override
    public void onRenderTickPre(@NonNull final RenderContext ctx) {
        EvictingQueue<CachedItem> items = ItemPickupQueue.getItems();
        if (!items.isEmpty()) {
            val x = ModulesConfig.ITEM_PICKUP_HUD.hudStyle.hasIcon() ? 17 : 4;
            val y = ctx.getScreenHeight() - (ctx.getFontHeight() * items.size()) - (2 * items.size());
            val iterator = items.iterator();

            for (var i = 0; iterator.hasNext(); ++i) {
                val cachedItem = iterator.next();
                val y1 = y + (ctx.getFontHeight() * i) + (2 * i);

                if (renderLabel(ctx, x, y1, cachedItem)) {
                    iterator.remove();
                }
            }
        }
    }

    private boolean renderLabel(
        @NonNull final RenderContext ctx,
        final float renderX,
        final float renderY,
        @NonNull final CachedItem item
    ) {
        val name = ModulesConfig.ITEM_PICKUP_HUD.hudStyle.hasLabel();
        val key = "label." + SmartHUD.ID + ".pickup." + (name ? "long" : "short");
        val count = StringHelper.getAbbreviatedValue(item.getCount());
        val label = I18n.format(key, count, item.getName());

        val color = 0xFFFFFF;
        val labelWidth = ctx.getStringWidth(label);
        var labelX = HandHelper.getSideOffset(renderX, labelWidth);
        var iconX = HandHelper.getSideOffset(renderX - 14.0F, 10.72F);

        if (HandHelper.isLeftHanded()) {
            labelX += ctx.getScreenWidth();
            iconX += ctx.getScreenHeight();
        }

        val remaining = item.getRemainingTicks(ModulesConfig.ITEM_PICKUP_HUD.displayTime);

        if (remaining < 0) {
            val time = Math.abs(remaining) + ctx.getPartialTicks();
            if (time > ItemPickupRender.ANIMATION_DURATION) return true;
            //            switch (ITEM_PICKUP_HUD.animationStyle) {
            //                case FADE:
            //                    float alpha = (float) (1.0F * Math.sin(time));
            //                    color |= (int) alpha << 24; // FIXME
            //                    break;
            //                case GLIDE:
            val end = renderX + labelWidth;
            val interpolation = ItemPickupRender.ANIMATION
                .interpolate(0, ItemPickupRender.ANIMATION_DURATION, time) * end;
            labelX += HandHelper.isLeftHanded() ? interpolation : -interpolation;
            iconX += HandHelper.isLeftHanded() ? interpolation : -interpolation;
            //                    break;
            //            }
        }

        ctx.drawString(label, labelX, renderY, color);

        if (ModulesConfig.ITEM_PICKUP_HUD.hudStyle.hasIcon()) {
            GlStateManager.enableAlpha();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(iconX, renderY - 1.5D, 0.0D);
            GlStateManager.scale(0.67D, 0.67D, 0.67D);
            ctx.renderItem(item.getStack(), 0, 0, true); // TODO: Support AnimationStyle#FADE
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }

        return false;
    }
}
