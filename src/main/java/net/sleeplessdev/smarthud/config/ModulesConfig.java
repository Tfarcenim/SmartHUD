package net.sleeplessdev.smarthud.config;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.data.HotbarStyle;
import net.sleeplessdev.smarthud.data.PickupStyle;

@UtilityClass
@Config(modid = SmartHUD.ID, name = SmartHUD.ID + "/modules", category = "")
@EventBusSubscriber(modid = SmartHUD.ID, value = Side.CLIENT)
public class ModulesConfig {
    @Name("hotbar")
    public final Hotbar HOTBAR_HUD = new Hotbar();

    @Name("item_pickup")
    public final ItemPickup ITEM_PICKUP_HUD = new ItemPickup();

    @SubscribeEvent
    void onConfigChanged(@NonNull final OnConfigChangedEvent event) {
        if (SmartHUD.ID.equals(event.getModID())) {
            ConfigManager.sync(SmartHUD.ID, Type.INSTANCE);
        }
    }

    public final class Hotbar {
        public boolean alwaysShow = false;
        public HotbarStyle hudStyle = HotbarStyle.OFFHAND;
        public boolean isEnabled = true;
        @Comment("Global override for duplicate merging. Items in the whitelist can override this")
        public boolean mergeDuplicates = true;
        public boolean renderOverlays = true;
        public boolean showStackSize = false;
        @RangeInt(min = 1, max = 9)
        public int slotLimit = 3;
    }

    public final class ItemPickup {
        //public PickupAnimation animationStyle = PickupAnimation.GLIDE;
        public int displayTime = 3000;
        public PickupStyle hudStyle = PickupStyle.BOTH;
        public boolean isEnabled = true;
        public int itemLimit = 10;
        @Comment({ "0: The most recently picked up item will be moved to the first slot",
                   "1: The order will remain the same, only item counts will be changed" })
        @RangeInt(min = 0, max = 1)
        public int priorityMode = 0;
    }
}
