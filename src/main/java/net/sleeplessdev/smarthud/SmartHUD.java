package net.sleeplessdev.smarthud;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.sleeplessdev.smarthud.config.WhitelistParser;
import net.sleeplessdev.smarthud.event.ItemPickupQueue;
import net.sleeplessdev.smarthud.util.CachedItem;
import net.sleeplessdev.smarthud.util.StackHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

@Mod(SmartHUD.ID)
public final class SmartHUD {
	public static final String ID = "smarthud";
	public static final String NAME = "Smart HUD";

	public SmartHUD() {
		INSTANCE = this;
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onPreInit);
		EVENT_BUS.addListener(this::onClientTick);
		EVENT_BUS.addListener(this::onPlayerTick);
	}

	public static SmartHUD INSTANCE;

	public static final Logger LOGGER = LogManager.getLogger(SmartHUD.NAME);

	public static File configPath;

	private void onPreInit(FMLClientSetupEvent event) {
		File dir = FMLPaths.CONFIGDIR.get().toFile();//event.getModConfigurationDirectory();
		configPath = new File(dir, SmartHUD.ID);
		if (!configPath.exists() && configPath.mkdirs()) {
			LOGGER.debug("Pre-generated configuration directory");
		}
		WhitelistParser.reload();
		ItemPickupQueue.initialize();
	}

	public static List<CachedItem> inventory = new ArrayList<>();

	@SubscribeEvent
	public void onPlayerTick(final TickEvent.PlayerTickEvent event) {
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

	public static long ticksElapsed;

	private void onClientTick(final TickEvent.ClientTickEvent event) {
		Minecraft client = Minecraft.getInstance();
		if (event.phase == TickEvent.Phase.END && !client.isGamePaused()) {
			ticksElapsed++;
		}
	}
}
