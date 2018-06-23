package net.sleeplessdev.smarthud.util;

import lombok.NonNull;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public interface IRenderEvent {
    boolean canRender();

    RenderGameOverlayEvent.ElementType getType();

    default void onRenderTickPre(@NonNull final RenderContext ctx) {}

    default void onRenderTickPost(@NonNull final RenderContext ctx) {}
}
