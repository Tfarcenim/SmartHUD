package net.sleeplessdev.smarthud.config;

//@Config(modid = SmartHUD.ID, name = SmartHUD.ID + "/general", category = "")
public final class GeneralConfig {
    public static final Whitelist WHITELIST = new Whitelist();

    public static final class Whitelist {
        public boolean isEnabled = true;
        public boolean logMissingEntries = false;
    }
}
