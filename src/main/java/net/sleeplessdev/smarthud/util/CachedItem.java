package net.sleeplessdev.smarthud.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.sleeplessdev.smarthud.config.ModulesConfig;

import java.util.function.IntPredicate;


public final class CachedItem {
    private final ItemStack stack;
    private final int actualCount;
    public int count;
    private long timestamp;

    public boolean ignoreNBT = true;
    public boolean ignoreDmg = true;

    private Boolean mergeDuplicates = null;

    private IntPredicate dimensionPredicate;

    public CachedItem(final ItemStack stack, final int count) {
        this.stack = stack.copy();
        this.actualCount = stack.getCount();
        this.stack.setCount(1);
        this.count = count;
        this.timestamp = TickListener.ticksElapsed;
        this.dimensionPredicate = i -> true;
    }

    public CachedItem(final ItemStack stack) {
        this(stack, 1);
    }

    public boolean isMergeDuplicates() {
        return mergeDuplicates != null ? mergeDuplicates : ModulesConfig.HOTBAR_HUD.mergeDuplicates;
    }

    public void setMergeDuplicates(final boolean mergeDuplicates) {
        this.mergeDuplicates = mergeDuplicates;
    }


    public void setDimensionPredicate(final IntPredicate predicate) {
        this.dimensionPredicate = predicate;
    }

    public void renewTimestamp() {
        timestamp = TickListener.ticksElapsed;
    }

    public ITextComponent getName() {
        return stack.getDisplayName();
    }

    public long getRemainingTicks(final int cooldown) {
        long time = TickListener.ticksElapsed;
        return (timestamp + cooldown / 50) - time;
    }

    public boolean matchesDimension(final int dimension) {
        return dimensionPredicate.test(dimension);
    }

    // TODO Clean this the fuck up
    public boolean matchesStack(final ItemStack stack, final boolean fuzzy) {
        ItemStack match = stack.copy();

        match.setCount(1);

        if (!fuzzy) {
            boolean isItemEqual = ignoreDmg
                              ? ItemStack.areItemsEqualIgnoreDurability(this.stack, match)
                              : ItemStack.areItemsEqual(this.stack, match);
            boolean isNBTEqual = ignoreNBT || ItemStack.areItemStackTagsEqual(this.stack, match);
            return isItemEqual && isNBTEqual;
        } else return ItemStack.areItemsEqualIgnoreDurability(this.stack, match);
    }
}
