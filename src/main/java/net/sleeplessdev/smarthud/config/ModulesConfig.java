package net.sleeplessdev.smarthud.config;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.api.distmarker.Dist;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.data.HotbarStyle;
import net.sleeplessdev.smarthud.data.PickupStyle;

@EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT)
public class ModulesConfig {

    public static final Hotbar HOTBAR_HUD = new Hotbar();

    public static final ItemPickup ITEM_PICKUP_HUD = new ItemPickup();

    @SubscribeEvent
    void onConfigChanged(final OnConfigChangedEvent event) {
        if (SmartHUD.ID.equals(event.getModID())) {
         //   ConfigManager.sync(SmartHUD.ID, Type.INSTANCE);
        }
    }

    public static final class Hotbar {
        public boolean alwaysShow = false;
        public HotbarStyle hudStyle = HotbarStyle.OFFHAND;
        public boolean isEnabled = true;
        //@Comment("Global override for duplicate merging. Items in the whitelist can override this")
        public boolean mergeDuplicates = true;
        public boolean renderOverlays = true;
        public boolean showStackSize = false;
       // @RangeInt(min = 1, max = 9)
        public int slotLimit = 3;
    }

    public static final class ItemPickup {
        //public PickupAnimation animationStyle = PickupAnimation.GLIDE;
        public int displayTime = 3000;
        public PickupStyle hudStyle = PickupStyle.BOTH;
        public boolean isEnabled = true;
        public int itemLimit = 10;
        //@Comment({ "0: The most recently picked up item will be moved to the first slot",
        //           "1: The order will remain the same, only item counts will be changed" })
        //@RangeInt(min = 0, max = 1)
        public int priorityMode = 0;
    }
}
