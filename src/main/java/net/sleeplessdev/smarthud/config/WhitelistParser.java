package net.sleeplessdev.smarthud.config;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
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
    public static void onConfigChanged(OnConfigChangedEvent event) {
        //if (SmartHUD.ID.equals(event.getModID())) {
            reload();
        //}
    }

    public static void reload() {
        if (!GeneralConfig.WHITELIST.isEnabled) {
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
                    DimensionType dim = DimensionType.getById(array.get(0).getAsInt());

                    if (isDimensionPresent(dim, i)) {
                        cachedItem.setDimensionPredicate(d -> d == dim);
                    } else cachedItem.setDimensionPredicate(d -> false);
                } else {
                    Set<DimensionType> dims = new HashSet<>();

                    for (JsonElement element : array) {
                        DimensionType dim = DimensionType.getById(element.getAsInt());

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

        if (!missingEntries.isEmpty() && GeneralConfig.WHITELIST.logMissingEntries) {
            SmartHUD.LOGGER.warn("Entries were skipped as the following items could not be found:");
            for (String entry : missingEntries) {
                SmartHUD.LOGGER.warn("-> " + entry);
            }
        }
    }

    private static boolean isDimensionPresent(final DimensionType dim, final int index) {
        if (/*DimensionManager.isDimensionRegistered(dim)*/true) return true;//todo
        SmartHUD.LOGGER.warn("Unregistered or invalid dimension {} found in whitelist entry at index {}", dim, index);
        return false;
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
