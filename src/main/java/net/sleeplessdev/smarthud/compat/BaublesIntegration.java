package net.sleeplessdev.smarthud.compat;

import baubles.api.BaublesApi;
import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;
import lombok.experimental.var;
import lombok.val;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.StackHelper;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@EventBusSubscriber(modid = SmartHUD.ID, value = Side.CLIENT)
public class BaublesIntegration {
    private List<CachedItem> baubles = new ArrayList<>();

    public ImmutableList<CachedItem> getBaubles() {
        return ImmutableList.copyOf(BaublesIntegration.baubles);
    }

    @SubscribeEvent
    @Optional.Method(modid = "baubles")
    void onPlayerTick(TickEvent.PlayerTickEvent event) {
        val handler = BaublesApi.getBaublesHandler(event.player);
        val dim = event.player.dimension;
        val baubleCache = new ArrayList<CachedItem>();
        for (var slot = 0; slot < handler.getSlots(); ++slot) {
            val bauble = handler.getStackInSlot(slot).copy();
            if (!bauble.isEmpty() && StackHelper.isWhitelisted(bauble, dim)) {
                StackHelper.process(baubleCache, bauble);
            }
        }
        BaublesIntegration.baubles = baubleCache;
    }
}
