package net.sleeplessdev.smarthud.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.StackHelper;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT)
public class InventoryCache {
    private static List<CachedItem> inventory = new ArrayList<>();

    @SubscribeEvent
    void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        List<ItemStack> inv = player.inventory.mainInventory;
        List<CachedItem> inventoryCache = new ArrayList<>();
        DimensionType dim = player.dimension;

        for (int slot = 9; slot < 36; ++slot) {
            ItemStack stack = inv.get(slot).copy();

            if (!stack.isEmpty() && StackHelper.isWhitelisted(stack, dim)) {
                StackHelper.process(inventoryCache, stack);
            }
        }

        inventory = inventoryCache;
    }

    public static ImmutableList<CachedItem> getInventory() {
        return ImmutableList.copyOf(inventory);
    }
}
