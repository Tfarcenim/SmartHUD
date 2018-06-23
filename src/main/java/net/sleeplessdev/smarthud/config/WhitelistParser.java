package net.sleeplessdev.smarthud.config;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.experimental.var;
import lombok.val;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.sleeplessdev.smarthud.SmartHUD;
import net.sleeplessdev.smarthud.util.CachedItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@UtilityClass
@EventBusSubscriber(modid = SmartHUD.ID, value = Side.CLIENT)
public final class WhitelistParser {
    private final List<CachedItem> ITEMS = new ArrayList<>();

    public ImmutableList<CachedItem> entries() {
        return ImmutableList.copyOf(WhitelistParser.ITEMS);
    }

    @SubscribeEvent
    void onConfigChanged(OnConfigChangedEvent event) {
        if (SmartHUD.ID.equals(event.getModID())) {
            reload();
        }
    }

    public void reload() {
        if (!GeneralConfig.WHITELIST.isEnabled) {
            ITEMS.clear();
            ITEMS.add(new CachedItem(new ItemStack(Items.CLOCK)));
            ITEMS.add(new CachedItem(new ItemStack(Items.COMPASS)));
            return;
        }

        val stopwatch = Stopwatch.createStarted();
        val missingEntries = new ArrayList<String>();

        final JsonElement file;

        try (val reader = new InputStreamReader(new FileInputStream(getOrGenerateJson()))) {
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

        for (var i = 0; i < entries.size(); ++i) {
            val json = entries.get(i).getAsJsonObject();

            if (json.isJsonNull() || !json.has("item")) {
                SmartHUD.LOGGER.warn("Whitelist entry at index {} is missing required value \"item\"", i);
                continue;
            }

            val id = new ResourceLocation(json.get("item").getAsString());
            val item = Item.REGISTRY.getObject(id);

            if (item == null) {
                if (Loader.isModLoaded(id.getResourceDomain())) {
                    SmartHUD.LOGGER.warn("Unable to find item for whitelist entry at index {} by name <{}>", i, id);
                } else if (!missingEntries.contains(id.getResourceDomain())) {
                    missingEntries.add(id.toString());
                }
                continue;
            }

            val cachedItem = new CachedItem(new ItemStack(item));

            if (json.has("meta")) {
                val meta = json.get("meta").getAsInt();

                if (meta < 0 || meta > Short.MAX_VALUE) {
                    SmartHUD.LOGGER.warn("Invalid metadata <{}> found in whitelist entry at index {}", meta, i);
                } else cachedItem.setMetadata(meta);
            }

            if (json.has("ignore_nbt")) {
                cachedItem.setIgnoreNBT(json.get("ignore_nbt").getAsBoolean());
            }

            if (json.has("ignore_dmg")) {
                cachedItem.setIgnoreDmg(json.get("ignore_dmg").getAsBoolean());
            }

            if (json.has("merge_duplicates")) {
                cachedItem.setMergeDuplicates(json.get("merge_duplicates").getAsBoolean());
            }

            if (json.has("dimensions")) {
                val array = json.get("dimensions").getAsJsonArray();

                if (array.size() == 1) {
                    val dim = array.get(0).getAsInt();

                    if (isDimensionPresent(dim, i)) {
                        cachedItem.setDimensionPredicate(d -> d == dim);
                    } else cachedItem.setDimensionPredicate(d -> false);
                } else {
                    val dims = new IntOpenHashSet();

                    for (val element : array) {
                        val dim = element.getAsInt();

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

        val time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        SmartHUD.LOGGER.info("Finished processing whitelist config in {}ms", time);

        if (!missingEntries.isEmpty() && GeneralConfig.WHITELIST.logMissingEntries) {
            SmartHUD.LOGGER.warn("Entries were skipped as the following items could not be found:");
            for (val entry : missingEntries) {
                SmartHUD.LOGGER.warn("-> " + entry);
            }
        }
    }

    private boolean isDimensionPresent(final int dim, final int index) {
        if (DimensionManager.isDimensionRegistered(dim)) return true;
        SmartHUD.LOGGER.warn("Unregistered or invalid dimension {} found in whitelist entry at index {}", dim, index);
        return false;
    }

    private File getOrGenerateJson() {
        val defaultWhitelist = new File(SmartHUD.getConfigPath(), "defaults.json");
        val userWhitelist = new File(SmartHUD.getConfigPath(), "whitelist.json");

        writeToFile(defaultWhitelist, true);

        if (!userWhitelist.exists()) {
            writeToFile(userWhitelist, false);
        }

        return userWhitelist;
    }

    private void writeToFile(@NonNull final File file, final boolean overwrite) {
        val path = "/assets/" + SmartHUD.ID + "/data/whitelist.json";

        try (val stream = SmartHUD.class.getResourceAsStream(path)) {
            if (overwrite) {
                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else Files.copy(stream, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
