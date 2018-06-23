package net.sleeplessdev.smarthud.config;

import lombok.experimental.UtilityClass;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.sleeplessdev.smarthud.SmartHUD;

@UtilityClass
@Config(modid = SmartHUD.ID, name = SmartHUD.ID + "/general", category = "")
public final class GeneralConfig {
    @Name("whitelist")
    public final Whitelist WHITELIST = new Whitelist();

    public final class Whitelist {
        public boolean isEnabled = true;
        public boolean logMissingEntries = false;
    }
}
