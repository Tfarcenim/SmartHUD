package net.sleeplessdev.smarthud.event;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.render.HotbarRender;
import net.sleeplessdev.smarthud.render.ItemPickupRender;
import net.sleeplessdev.smarthud.util.IRenderEvent;
import net.sleeplessdev.smarthud.util.RenderContext;

import java.util.List;

@UtilityClass
@Mod.EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT)
public class RenderManager {
    private final List<IRenderEvent> EVENTS = Lists.newArrayList(new HotbarRender(), new ItemPickupRender());

    @SubscribeEvent
    void onRenderGameOverlayPre(@NonNull final RenderGameOverlayEvent.Pre event) {
        val ctx = new RenderContext(FMLClientHandler.instance().getClient(), event);

        for (val renderEvent : RenderManager.EVENTS) {
            if (canRender(renderEvent, event)) renderEvent.onRenderTickPre(ctx);
        }
    }

    @SubscribeEvent
    void onRenderGameOverlayPost(@NonNull final RenderGameOverlayEvent.Post event) {
        val mc = FMLClientHandler.instance().getClient();
        val ctx = new RenderContext(mc, event);

        for (val renderEvent : RenderManager.EVENTS) {
            if (canRender(renderEvent, event)) renderEvent.onRenderTickPost(ctx);
        }
    }

    boolean canRender(@NonNull final IRenderEvent renderEvent, @NonNull final RenderGameOverlayEvent event) {
        return renderEvent.canRender() && renderEvent.getType() == event.getType();
    }
}
