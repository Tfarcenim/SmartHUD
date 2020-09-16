package net.sleeplessdev.smarthud.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.data.HotbarStyle;
import net.sleeplessdev.smarthud.data.PickupStyle;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModulesConfig {
	public static ForgeConfigSpec.BooleanValue isEnabled;
	public static ForgeConfigSpec.BooleanValue logMissingEntries;

	//hotbar
	public static ForgeConfigSpec.BooleanValue alwaysShow;
	public static HotbarStyle hotbarStyle = HotbarStyle.OFFHAND;
	public static ForgeConfigSpec.BooleanValue enableHotbar;
	//@Comment("Global override for duplicate merging. Items in the whitelist can override this")
	public static ForgeConfigSpec.BooleanValue mergeDuplicates;
	public static ForgeConfigSpec.BooleanValue renderOverlays;
	public static ForgeConfigSpec.BooleanValue showStackSize;

	public static ForgeConfigSpec.IntValue slotLimit;


	public static final ModulesConfig CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		final Pair<ModulesConfig, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(ModulesConfig::new);
		CLIENT_SPEC = specPair2.getRight();
		CLIENT = specPair2.getLeft();
	}

	public ModulesConfig(ForgeConfigSpec.Builder builder) {
		builder.push("general");
		isEnabled = builder.define("isEnabled", true);
		logMissingEntries = builder.define("logMissingEntries", false);

		builder.push("hotbar");
		alwaysShow = builder.define("alwaysShow",false);
		enableHotbar = builder.define("enableHotbar",true);
		mergeDuplicates = builder.define("mergeDuplicates",true);
		renderOverlays = builder.define("renderOverlays",true);
		showStackSize = builder.define("showStacksize",false);
		slotLimit = builder.defineInRange("slotLimit",3,1,27);
		xOffset = builder.defineInRange("xOffset",0,Integer.MIN_VALUE,Integer.MAX_VALUE);
		yOffset = builder.defineInRange("yOffset",-22,Integer.MIN_VALUE,Integer.MAX_VALUE);

		builder.pop();

		builder.push("itempickup");
		displayTime = builder.defineInRange("displayTime",3000,0,Integer.MAX_VALUE);
		itemPickupEnabled = builder.define("itemPickupIsEnabled",true);
		itemLimit = builder.defineInRange("itemLimit",10,0,Integer.MAX_VALUE);
		priorityMode = builder.defineInRange("priorityMode",0,0,1);
		builder.pop(2);
	}

		//public PickupAnimation animationStyle = PickupAnimation.GLIDE;
		public static ForgeConfigSpec.IntValue displayTime;
		public static PickupStyle itemPickupStyle = PickupStyle.BOTH;
		public static ForgeConfigSpec.BooleanValue itemPickupEnabled;
		public static ForgeConfigSpec.IntValue itemLimit;
		public static ForgeConfigSpec.IntValue xOffset;
	public static ForgeConfigSpec.IntValue yOffset;
	//@Comment({ "0: The most recently picked up item will be moved to the first slot",
		//           "1: The order will remain the same, only item counts will be changed" })
		//@RangeInt(min = 0, max = 1)
		public static ForgeConfigSpec.IntValue priorityMode;
}
