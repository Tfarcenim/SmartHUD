package net.sleeplessdev.smarthud;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.InstanceFactory;
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
    acceptedMinecraftVersions = "[1.11,1.13)",
    clientSideOnly = true
)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SmartHUD {
    public static final String ID = "smarthud";
    public static final String NAME = "Smart HUD";
    public static final String VERSION = "%VERSION%";

    @Getter(onMethod = @__({ @InstanceFactory, @Deprecated }))
    public static final SmartHUD INSTANCE = new SmartHUD();

    public static final Logger LOGGER = LogManager.getLogger(SmartHUD.NAME);

    @Getter private static File configPath;

    @EventHandler
    void onPreInit(@NonNull final FMLPreInitializationEvent event) {
        @NonNull val dir = event.getModConfigurationDirectory();
        configPath = new File(dir, SmartHUD.ID);
        if (!configPath.exists() && configPath.mkdirs()) {
            LOGGER.debug("Pre-generated configuration directory");
        }
    }

    @EventHandler
    void onPostInit(@NonNull final FMLPostInitializationEvent event) {
        WhitelistParser.reload();
        ItemPickupQueue.initialize();
    }
}
