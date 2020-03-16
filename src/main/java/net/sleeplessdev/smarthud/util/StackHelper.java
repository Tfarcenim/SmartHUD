package net.sleeplessdev.smarthud.util;

import net.minecraft.item.ItemStack;
import net.sleeplessdev.smarthud.config.WhitelistParser;

import java.util.List;

public class StackHelper {
    public boolean isWhitelisted(final ItemStack stack, final int dimension) {
        return WhitelistParser.entries().stream().anyMatch(item -> item.matchesStack(stack, true) && item.matchesDimension(dimension));
    }

    public void process(final List<CachedItem> cache, final ItemStack stack) {
        int count = stack.getCount();
        boolean shouldCache = true;

        for (CachedItem item : cache) {
            if (item.matchesStack(stack, false) && item.isMergeDuplicates()) {
                item.count += count;
                shouldCache = false;
                break;
            }
        }

        if (shouldCache) {
            cache.add(new CachedItem(stack, count));
        }
    }
}
