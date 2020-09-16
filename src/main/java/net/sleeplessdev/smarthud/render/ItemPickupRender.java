package net.sleeplessdev.smarthud.render;

import com.google.common.collect.EvictingQueue;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.config.ModulesConfig;
import net.sleeplessdev.smarthud.event.ItemPickupQueue;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.HandHelper;
import net.sleeplessdev.smarthud.util.RenderEvent;
import net.sleeplessdev.smarthud.util.RenderContext;
import net.sleeplessdev.smarthud.util.StringHelper;
import net.sleeplessdev.smarthud.util.interpolation.CubicBezierInterpolator;
import net.sleeplessdev.smarthud.util.interpolation.Interpolator;

import java.util.Iterator;

public final class ItemPickupRender implements RenderEvent {
    private static final Interpolator ANIMATION = new CubicBezierInterpolator(0.42D, 0.0D, 0.58D, 1.0D);
    private static final float ANIMATION_DURATION = 10.0F;

    @Override
    public boolean canRender() {
        return ModulesConfig.itemPickupEnabled.get();
    }

    @Override
    public RenderGameOverlayEvent.ElementType getType() {
        return RenderGameOverlayEvent.ElementType.TEXT;
    }

    @Override
    public void onRenderTickPre(final RenderContext ctx) {
        EvictingQueue<CachedItem> items = ItemPickupQueue.items;
        if (!items.isEmpty()) {
            int x = ModulesConfig.itemPickupStyle.hasIcon ? 17 : 4;
            int y = ctx.screenHeight - (ctx.getFontHeight() * items.size()) - (2 * items.size());
            Iterator<CachedItem> iterator = items.iterator();

            for (int i = 0; iterator.hasNext(); ++i) {
                CachedItem cachedItem = iterator.next();
                int y1 = y + (ctx.getFontHeight() * i) + (2 * i);

                if (renderLabel(ctx, x, y1, cachedItem)) {
                    iterator.remove();
                }
            }
        }
    }

    private boolean renderLabel(
        final RenderContext ctx,
        final float renderX,
        final float renderY,
        final CachedItem item
    ) {
        boolean name = ModulesConfig.itemPickupStyle.hasLabel;
        String key = "label." + SmartHUD.ID + ".pickup." + (name ? "long" : "short");
        String count = StringHelper.getAbbreviatedValue(item.count);
        String label = I18n.format(key, count, item.getName().getUnformattedComponentText());

        int color = 0xFFFFFF;
        int labelWidth = ctx.getStringWidth(label);
        float labelX = HandHelper.getSideOffset(renderX, labelWidth);
        float iconY = HandHelper.getSideOffset(renderX - 14.0F, 10.72F);

        if (HandHelper.isLeftHanded()) {
            labelX += ctx.screenWidth;
            iconY += ctx.screenHeight;
        }

        float remaining = item.getRemainingTicks(ModulesConfig.displayTime.get());

        if (remaining < 0) {
            float time = Math.abs(remaining) + ctx.partialTicks;
            if (time > ItemPickupRender.ANIMATION_DURATION) return true;
            //            switch (ITEM_PICKUP_HUD.animationStyle) {
            //                case FADE:
            //                    float alpha = (float) (1.0F * Math.sin(time));
            //                    color |= (int) alpha << 24; // FIXME
            //                    break;
            //                case GLIDE:
            float end = renderX + labelWidth;
            float interpolation = ItemPickupRender.ANIMATION
                .interpolate(0, ItemPickupRender.ANIMATION_DURATION, time) * end;
            labelX += HandHelper.isLeftHanded() ? interpolation : -interpolation;
            iconY += HandHelper.isLeftHanded() ? interpolation : -interpolation;
            //                    break;
            //            }
        }

        ctx.drawStringWithShadow(ctx.matrices,label, labelX, renderY, color);

        if (ModulesConfig.itemPickupStyle.hasIcon) {
            RenderSystem.enableBlend();
            RenderHelper.enableStandardItemLighting();
            RenderSystem.pushMatrix();
            RenderSystem.translated(iconY, renderY - 1.5D, 0.0D);
            RenderSystem.scaled(0.67D, 0.67D, 0.67D);
            ctx.renderItem(item.stack, 0, 0, true); // TODO: Support AnimationStyle#FADE
            RenderSystem.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
        return false;
    }
}
