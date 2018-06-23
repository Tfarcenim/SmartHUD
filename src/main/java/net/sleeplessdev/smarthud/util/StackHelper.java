package net.sleeplessdev.smarthud.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.experimental.var;
import lombok.val;
import net.minecraft.item.ItemStack;
import net.sleeplessdev.smarthud.config.WhitelistParser;

import java.util.List;

@UtilityClass
public class StackHelper {
    public boolean isWhitelisted(@NonNull final ItemStack stack, final int dimension) {
        for (val item : WhitelistParser.entries()) {
            if (item.matchesStack(stack, true) && item.matchesDimension(dimension)) {
                return true;
            }
        }
        return false;
    }

    public void process(@NonNull final List<CachedItem> cache, @NonNull final ItemStack stack) {
        val count = stack.getCount();
        var shouldCache = true;

        for (val item : cache) {
            if (item.matchesStack(stack, false) && item.isMergeDuplicates()) {
                item.setCount(item.getCount() + count);
                shouldCache = false;
                break;
            }
        }

        if (shouldCache) {
            cache.add(new CachedItem(stack, count));
        }
    }
}
