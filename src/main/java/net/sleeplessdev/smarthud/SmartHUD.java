package net.sleeplessdev.smarthud;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.sleeplessdev.smarthud.config.WhitelistParser;
import net.sleeplessdev.smarthud.event.ItemPickupQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
    modid = SmartHUD.ID,
    name = SmartHUD.NAME,
    version = SmartHUD.VERSION,
    dependencies = SmartHUD.DEPENDENCIES,
    acceptedMinecraftVersions = SmartHUD.MC_VERSIONS,
    clientSideOnly = true
)
public final class SmartHUD {
    public static final String ID = "smarthud";
    public static final String NAME = "Smart HUD";
    public static final String VERSION = "%VERSION%";
    public static final String DEPENDENCIES = "after:*";
    public static final String MC_VERSIONS = "[1.11,1.13)";

    public static final Logger LOGGER = LogManager.getLogger(SmartHUD.NAME);

    private static File configPath;

    public static File getConfigPath() {
        return SmartHUD.configPath;
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        SmartHUD.configPath = new File(event.getModConfigurationDirectory(), SmartHUD.ID);
        if (!SmartHUD.configPath.exists() && SmartHUD.configPath.mkdirs()) {
            SmartHUD.LOGGER.debug("Pre-generated configuration directory");
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        WhitelistParser.reloadWhitelistEntries();
        ItemPickupQueue.initialize();
    }
}
