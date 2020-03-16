package net.sleeplessdev.smarthud.event;

import com.google.common.base.Throwables;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ForwardingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.util.CachedItem;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Queue;

import static net.sleeplessdev.smarthud.config.ModulesConfig.ITEM_PICKUP_HUD;

@EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT)
public class ItemPickupQueue {
    public static EvictingQueue<CachedItem> items =
        EvictingQueue.create(ITEM_PICKUP_HUD.itemLimit);

    private static boolean init = false;

    public static void initialize() {
        if (!init) {
            initializeParticleQueue();
            init = true;
        }
        reloadQueue();
    }

    private static EvictingQueue<CachedItem> createNewQueue() {
        return EvictingQueue.create(ITEM_PICKUP_HUD.itemLimit);
    }

    private static void reloadQueue() {
        EvictingQueue<CachedItem> newQueue = createNewQueue();
        if (newQueue.addAll(ItemPickupQueue.items)) {
            ItemPickupQueue.items = newQueue;
        } else throw new IllegalStateException("Unable to populate new queue");
    }

    @SubscribeEvent
    void onConfigChanged(final OnConfigChangedEvent event) {
        if (SmartHUD.ID.equals(event.getModID())) {
            reloadQueue();
        }
    }

    private static void initializeParticleQueue() {
        try {
            Field field = ObfuscationReflectionHelper.findField(ParticleManager.class, "field_187241_h");//"queue"
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle itemGetter = getParticleItemPickupGetter(lookup, "field_174840_a");//"item"
            MethodHandle targetGetter = getParticleItemPickupGetter(lookup, "field_174843_ax");//"target"
            ParticleManager particleManager = Minecraft.getInstance().particles;
            @SuppressWarnings("unchecked") Queue<Particle> newQueue = (Queue<Particle>) field.get(particleManager);
            field.set(particleManager, createForwardingParticleQueue(newQueue, itemGetter, targetGetter));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle getParticleItemPickupGetter(final MethodHandles.Lookup lookup, final String fieldName
    ) throws IllegalAccessException {
        return lookup.unreflectGetter(ObfuscationReflectionHelper.findField(ItemPickupParticle.class, fieldName));
    }

    private static Queue<Particle> createForwardingParticleQueue(
            final Queue<Particle> delegate,
        final MethodHandle itemGetter,
        final MethodHandle targetGetter
    ) {
        return new ForwardingQueue<Particle>() {
            @Override
            protected Queue<Particle> delegate() {
                return delegate;
            }

            @Override
            public boolean add(@Nullable final Particle particle) {
                if (!super.add(particle)) return false;
                if (particle != null && ItemPickupParticle.class.equals(particle.getClass())) {
                    final Entity item;
                    final Entity target;
                    try {
                        item = (Entity) itemGetter.invoke(particle);
                        target = (Entity) targetGetter.invoke(particle);
                    } catch (Throwable e) {
                        Throwables.throwIfUnchecked(e);
                        throw new RuntimeException(e);
                    }
                    if (item instanceof ItemEntity && target instanceof ClientPlayerEntity) {
                        handleItemCollection(((ItemEntity) item).getItem());
                    }
                }
                return true;
            }
        };
    }

    private static void handleItemCollection(final ItemStack stack) {
        if (stack.isEmpty()) return;
        EvictingQueue<CachedItem> newItems = createNewQueue();
        newItems.addAll(ItemPickupQueue.items);
        if (ItemPickupQueue.items.isEmpty()) {
            newItems.add(new CachedItem(stack, stack.getCount()));
        } else {
            boolean shouldCache = true;
            for (CachedItem cachedItem : ItemPickupQueue.items) {
                if (cachedItem.matchesStack(stack, true)) {
                    int count = cachedItem.count + stack.getCount();
                    if (ITEM_PICKUP_HUD.priorityMode == 0) {
                        newItems.remove(cachedItem);
                        newItems.add(new CachedItem(stack, count));
                        shouldCache = false;
                    } else if (ITEM_PICKUP_HUD.priorityMode == 1) {
                        cachedItem.count = count;
                        cachedItem.renewTimestamp();
                        shouldCache = false;
                    }
                    break;
                }
            }
            if (shouldCache) {
                newItems.add(new CachedItem(stack, stack.getCount()));
            }
        }

        ItemPickupQueue.items = newItems;
    }
}
