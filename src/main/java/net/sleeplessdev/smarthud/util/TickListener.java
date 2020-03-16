package net.sleeplessdev.smarthud.util;


import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.sleeplessdev.smarthud.SmartHUD;

@EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT)
public class TickListener {

    public static long ticksElapsed;

    @SubscribeEvent
    void onClientTick(final TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.END && !client.isGamePaused()) {
            TickListener.ticksElapsed++;
        }
    }
}
