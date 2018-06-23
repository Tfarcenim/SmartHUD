package net.sleeplessdev.smarthud.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.sleeplessdev.smarthud.SmartHUD;

@UtilityClass
@EventBusSubscriber(modid = SmartHUD.ID, value = Side.CLIENT)
public class TickListener {
    @Getter
    private static long ticksElapsed;

    @SubscribeEvent
    void onClientTick(@NonNull final ClientTickEvent event) {
        @NonNull val client = FMLClientHandler.instance().getClient();
        if (event.phase == Phase.END && !client.isGamePaused()) {
            TickListener.ticksElapsed++;
        }
    }
}
