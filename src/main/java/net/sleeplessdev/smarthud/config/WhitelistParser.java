package net.sleeplessdev.smarthud.config;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.util.CachedItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber(modid = SmartHUD.ID, value = Dist.CLIENT,bus = EventBusSubscriber.Bus.MOD)
public final class WhitelistParser {
    private static final List<CachedItem> ITEMS = new ArrayList<>();

    public static ImmutableList<CachedItem> entries() {
        return ImmutableList.copyOf(WhitelistParser.ITEMS);
    }

    @SubscribeEvent
    public static void onConfigChanged(ModConfig.ModConfigEvent event) {
        if (SmartHUD.ID.equals(event.getConfig().getModId())) {
            reload();
        }
    }

    public static void reload() {
        if (!ModulesConfig.isEnabled.get()) {
            ITEMS.clear();
            ITEMS.add(new CachedItem(new ItemStack(Items.CLOCK)));
            ITEMS.add(new CachedItem(new ItemStack(Items.COMPASS)));
            return;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<String> missingEntries = new ArrayList<>();

        final JsonElement file;

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(getOrGenerateJson()))) {
            file = new JsonParser().parse(reader);
        } catch (IOException e) {
            SmartHUD.LOGGER.warn("Failed to parse whitelist config! Please report this to the mod author.");
            e.printStackTrace();
            return;
        }

        ITEMS.clear();

        final JsonArray entries;

        try {
            entries = file.getAsJsonArray();
        } catch (IllegalStateException e) {
            SmartHUD.LOGGER.warn("Received invalid data from the whitelist, please check your formatting!");
            return;
        }

        for (int i = 0; i < entries.size(); ++i) {
            JsonObject json = entries.get(i).getAsJsonObject();

            if (json.isJsonNull() || !json.has("item")) {
                SmartHUD.LOGGER.warn("Whitelist entry at index {} is missing required value \"item\"", i);
                continue;
            }

            ResourceLocation id = new ResourceLocation(json.get("item").getAsString());
            Item item = Registry.ITEM.getOrDefault(id);

            if (item == Items.AIR) {
                if (ModList.get().isLoaded(id.getNamespace())) {
                    SmartHUD.LOGGER.warn("Unable to find item for whitelist entry at index {} by name <{}>", i, id);
                } else if (!missingEntries.contains(id.getNamespace())) {
                    missingEntries.add(id.toString());
                }
                continue;
            }

            CachedItem cachedItem = new CachedItem(new ItemStack(item));

            if (json.has("ignore_nbt")) {
                cachedItem.ignoreNBT = json.get("ignore_nbt").getAsBoolean();
            }

            if (json.has("ignore_dmg")) {
                cachedItem.ignoreDmg = json.get("ignore_dmg").getAsBoolean();
            }

            if (json.has("merge_duplicates")) {
                cachedItem.setMergeDuplicates(json.get("merge_duplicates").getAsBoolean());
            }

            if (json.has("dimensions")) {
                JsonArray array = json.get("dimensions").getAsJsonArray();

                if (array.size() == 1) {
                    String dim = array.get(0).getAsString();

                    if (isDimensionPresent(dim, i)) {
                        cachedItem.setDimensionPredicate(d -> d .equals( dim));
                    } else cachedItem.setDimensionPredicate(d -> false);
                } else {
                    Set<String> dims = new HashSet<>();

                    for (JsonElement element : array) {
                        String dim = element.getAsString();

                        if (isDimensionPresent(dim, i)) {
                            dims.add(dim);
                        }
                    }
                    cachedItem.setDimensionPredicate(dims::contains);
                }
            }

            if (!ITEMS.contains(cachedItem)) {
                ITEMS.add(cachedItem);
            }
        }

        long time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        SmartHUD.LOGGER.info("Finished processing whitelist config in {}ms", time);

        if (!missingEntries.isEmpty() && ModulesConfig.logMissingEntries.get()) {
            SmartHUD.LOGGER.warn("Entries were skipped as the following items could not be found:");
            for (String entry : missingEntries) {
                SmartHUD.LOGGER.warn("-> " + entry);
            }
        }
    }

    /*public static boolean isDimensionRegistered(final String dim) {
        ResourceLocation id = new ResourceLocation(dim);
        //return Registry.DIMENSION_TYPE_KEY.getValue(id).isPresent();
    }*/

    private static boolean isDimensionPresent(final String dim, final int index) {
         return true;
        //SmartHUD.LOGGER.warn("Unregistered or invalid dimension {} found in whitelist entry at index {}", dim, index);
       // return false;
    }

    private static File getOrGenerateJson() {
        File defaultWhitelist = new File(SmartHUD.configPath, "defaults.json");
        File userWhitelist = new File(SmartHUD.configPath, "whitelist.json");

        writeToFile(defaultWhitelist, true);

        if (!userWhitelist.exists()) {
            writeToFile(userWhitelist, false);
        }

        return userWhitelist;
    }

    private static void writeToFile(final File file, final boolean overwrite) {
        String path = "/assets/" + SmartHUD.ID + "/data/whitelist.json";

        try (InputStream stream = SmartHUD.class.getResourceAsStream(path)) {
            if (overwrite) {
                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else Files.copy(stream, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
