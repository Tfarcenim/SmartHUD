package net.sleeplessdev.smarthud.event;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.render.HotbarRender;
import net.sleeplessdev.smarthud.render.ItemPickupRender;
import net.sleeplessdev.smarthud.util.RenderEvent;
import net.sleeplessdev.smarthud.util.RenderContext;

import java.util.List;

@Mod.EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT)
public class RenderManager {
    private static final List<RenderEvent> EVENTS = Lists.newArrayList(new HotbarRender(), new ItemPickupRender());

    @SubscribeEvent
    void onRenderGameOverlayPre(final RenderGameOverlayEvent.Pre event) {
        RenderContext ctx = new RenderContext(Minecraft.getInstance(), event);

        for (RenderEvent renderEvent : RenderManager.EVENTS) {
            if (canRender(renderEvent, event)) renderEvent.onRenderTickPre(ctx);
        }
    }

    @SubscribeEvent
    void onRenderGameOverlayPost(final RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        RenderContext ctx = new RenderContext(mc, event);

        for (RenderEvent renderEvent : RenderManager.EVENTS) {
            if (canRender(renderEvent, event)) renderEvent.onRenderTickPost(ctx);
        }
    }

    boolean canRender(final RenderEvent renderEvent, final RenderGameOverlayEvent event) {
        return renderEvent.canRender() && renderEvent.getType() == event.getType();
    }
}
