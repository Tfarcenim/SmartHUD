package net.sleeplessdev.smarthud.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.compat.BaublesIntegration;
import net.sleeplessdev.smarthud.config.ModulesConfig;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.StackHelper;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = SmartHUD.ID, value = Side.CLIENT)
public final class InventoryCache {
    private static List<CachedItem> inventory = new ArrayList<>();

    private InventoryCache() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        final boolean merge = ModulesConfig.HOTBAR_HUD.mergeDuplicates;
        final int dim = event.player.dimension;
        final List<ItemStack> inv = event.player.inventory.mainInventory;
        final List<CachedItem> inventoryCache = new ArrayList<>();

        for (int slot = 9; slot < 36; ++slot) {
            final ItemStack stack = inv.get(slot).copy();

            if (!stack.isEmpty() && StackHelper.isWhitelisted(stack, dim)) {
                StackHelper.processStack(inventoryCache, stack, merge);
            }
        }
        final List<CachedItem> baubles = BaublesIntegration.getBaubles();

        if (!baubles.isEmpty()) inventoryCache.addAll(baubles);

       InventoryCache.inventory = inventoryCache;
    }

    public static ImmutableList<CachedItem> getInventory() {
        return ImmutableList.copyOf(InventoryCache.inventory);
    }
}
