package net.sleeplessdev.smarthud.event;

import com.google.common.base.Throwables;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ForwardingQueue;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.experimental.var;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleItemPickup;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.util.CachedItem;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Queue;

import static net.sleeplessdev.smarthud.config.ModulesConfig.ITEM_PICKUP_HUD;

@UtilityClass
@EventBusSubscriber(modid = SmartHUD.ID, value = Side.CLIENT)
public class ItemPickupQueue {
    @Getter private EvictingQueue<CachedItem> items =
        EvictingQueue.create(ITEM_PICKUP_HUD.itemLimit);

    private boolean init = false;

    public void initialize() {
        if (!init) {
            initializeParticleQueue();
            init = true;
        }
        reloadQueue();
    }

    private EvictingQueue<CachedItem> createNewQueue() {
        return EvictingQueue.create(ITEM_PICKUP_HUD.itemLimit);
    }

    private void reloadQueue() {
        val newQueue = createNewQueue();
        if (newQueue.addAll(ItemPickupQueue.items)) {
            ItemPickupQueue.items = newQueue;
        } else throw new IllegalStateException("Unable to populate new queue");
    }

    @SubscribeEvent
    void onConfigChanged(@NonNull final OnConfigChangedEvent event) {
        if (SmartHUD.ID.equals(event.getModID())) {
            reloadQueue();
        }
    }

    private void initializeParticleQueue() {
        try {
            val field = ReflectionHelper.findField(ParticleManager.class, "field_187241_h", "queue");
            val lookup = MethodHandles.lookup();
            val itemGetter = getParticleItemPickupGetter(lookup, "field_174840_a", "item");
            val targetGetter = getParticleItemPickupGetter(lookup, "field_174843_ax", "target");
            val particleManager = Minecraft.getMinecraft().effectRenderer;
            @SuppressWarnings("unchecked") val newQueue = (Queue<Particle>) field.get(particleManager);
            field.set(particleManager, createForwardingParticleQueue(newQueue, itemGetter, targetGetter));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private MethodHandle getParticleItemPickupGetter(
        @NonNull final MethodHandles.Lookup lookup,
        @NonNull final String... fieldNames
    ) throws IllegalAccessException {
        return lookup.unreflectGetter(ReflectionHelper.findField(ParticleItemPickup.class, fieldNames));
    }

    private Queue<Particle> createForwardingParticleQueue(
        @NonNull final Queue<Particle> delegate,
        @NonNull final MethodHandle itemGetter,
        @NonNull final MethodHandle targetGetter
    ) {
        return new ForwardingQueue<Particle>() {
            @Override
            protected Queue<Particle> delegate() {
                return delegate;
            }

            @Override
            public boolean add(@Nullable final Particle particle) {
                if (!super.add(particle)) return false;
                if (particle != null && ParticleItemPickup.class.equals(particle.getClass())) {
                    @NonNull final Entity item;
                    @NonNull final Entity target;
                    try {
                        item = (Entity) itemGetter.invoke(particle);
                        target = (Entity) targetGetter.invoke(particle);
                    } catch (Throwable e) {
                        Throwables.throwIfUnchecked(e);
                        throw new RuntimeException(e);
                    }
                    if (item instanceof EntityItem && target instanceof EntityPlayerSP) {
                        handleItemCollection(((EntityItem) item).getItem());
                    }
                }
                return true;
            }
        };
    }

    private void handleItemCollection(@NonNull final ItemStack stack) {
        if (stack.isEmpty()) return;
        val newItems = createNewQueue();
        newItems.addAll(ItemPickupQueue.items);
        if (ItemPickupQueue.items.isEmpty()) {
            newItems.add(new CachedItem(stack, stack.getCount()));
        } else {
            var shouldCache = true;
            for (val cachedItem : ItemPickupQueue.items) {
                if (cachedItem.matchesStack(stack, true)) {
                    val count = cachedItem.getCount() + stack.getCount();
                    if (ITEM_PICKUP_HUD.priorityMode == 0) {
                        newItems.remove(cachedItem);
                        newItems.add(new CachedItem(stack, count));
                        shouldCache = false;
                    } else if (ITEM_PICKUP_HUD.priorityMode == 1) {
                        cachedItem.setCount(count);
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
