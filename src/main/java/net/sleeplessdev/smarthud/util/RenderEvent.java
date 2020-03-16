package net.sleeplessdev.smarthud.util;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

public interface RenderEvent {
    boolean canRender();

    RenderGameOverlayEvent.ElementType getType();

    default void onRenderTickPre(final RenderContext ctx) {}

    default void onRenderTickPost(final RenderContext ctx) {}
}
