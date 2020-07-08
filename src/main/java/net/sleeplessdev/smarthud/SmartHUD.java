package net.sleeplessdev.smarthud;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.config.ModConfig;
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
import static net.sleeplessdev.smarthud.config.ModulesConfig.CLIENT_SPEC;

@Mod(SmartHUD.ID)
public final class SmartHUD {
	public static final String ID = "smarthud";
	public static final String NAME = "Smarter HUD";

	public SmartHUD() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onPreInit);
		EVENT_BUS.addListener(this::onClientTick);
		EVENT_BUS.addListener(this::onPlayerTick);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
	}


	public static final Logger LOGGER = LogManager.getLogger(SmartHUD.NAME);

	public static File configPath;

	private void onPreInit(FMLClientSetupEvent event) {
		File dir = FMLPaths.CONFIGDIR.get().toFile();
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
		String dim = player.world.func_234922_V_().func_240901_a_().toString();

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
