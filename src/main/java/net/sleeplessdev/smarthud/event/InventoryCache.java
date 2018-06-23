package net.sleeplessdev.smarthud.event;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.experimental.var;
import lombok.val;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.compat.BaublesIntegration;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.StackHelper;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@EventBusSubscriber(modid = SmartHUD.ID, value = Side.CLIENT)
public class InventoryCache {
    private List<CachedItem> inventory = new ArrayList<>();

    @SubscribeEvent
    void onPlayerTick(@NonNull final TickEvent.PlayerTickEvent event) {
        @NonNull val player = event.player;
        @NonNull val inv = player.inventory.mainInventory;
        val inventoryCache = new ArrayList<CachedItem>();
        val dim = player.dimension;

        for (var slot = 9; slot < 36; ++slot) {
            val stack = inv.get(slot).copy();

            if (!stack.isEmpty() && StackHelper.isWhitelisted(stack, dim)) {
                StackHelper.process(inventoryCache, stack);
            }
        }

        val baubles = BaublesIntegration.getBaubles();

        if (!baubles.isEmpty()) {
            inventoryCache.addAll(baubles);
        }

        inventory = inventoryCache;
    }

    public ImmutableList<CachedItem> getInventory() {
        return ImmutableList.copyOf(inventory);
    }
}
