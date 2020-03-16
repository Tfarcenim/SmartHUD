package net.sleeplessdev.smarthud;

import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.sleeplessdev.smarthud.config.WhitelistParser;
import net.sleeplessdev.smarthud.event.ItemPickupQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(SmartHUD.ID)
public final class SmartHUD {
	public static final String ID = "smarthud";
	public static final String NAME = "Smart HUD";
	public static final String VERSION = "%VERSION%";

	public SmartHUD() {
		INSTANCE = this;
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onPreInit);
	}

	public static SmartHUD INSTANCE;

	public static final Logger LOGGER = LogManager.getLogger(SmartHUD.NAME);

	public static File configPath;

	void onPreInit(FMLClientSetupEvent event) {
		File dir = event.getModConfigurationDirectory();
		configPath = new File(dir, SmartHUD.ID);
		if (!configPath.exists() && configPath.mkdirs()) {
			LOGGER.debug("Pre-generated configuration directory");
		}
		WhitelistParser.reload();
		ItemPickupQueue.initialize();

	}
}
