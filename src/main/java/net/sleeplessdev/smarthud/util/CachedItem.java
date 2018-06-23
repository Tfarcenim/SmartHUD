package net.sleeplessdev.smarthud.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.sleeplessdev.smarthud.config.ModulesConfig;

import java.util.function.IntPredicate;


public final class CachedItem {
    @Getter private final ItemStack stack;
    @Getter private final int actualCount;
    @Getter private int meta = OreDictionary.WILDCARD_VALUE;
    @Getter @Setter private int count;
    @Getter private long timestamp;

    @Getter @Setter private boolean ignoreNBT = true;
    @Getter @Setter private boolean ignoreDmg = true;

    private Boolean mergeDuplicates = null;

    private IntPredicate dimensionPredicate;

    public CachedItem(@NonNull final ItemStack stack, final int count) {
        this.stack = stack.copy();
        this.actualCount = stack.getCount();
        this.stack.setCount(1);
        this.count = count;
        this.timestamp = TickListener.getTicksElapsed();
        this.dimensionPredicate = i -> true;
    }

    public CachedItem(@NonNull final ItemStack stack) {
        this(stack, 1);
    }

    public boolean isMergeDuplicates() {
        return mergeDuplicates != null ? mergeDuplicates : ModulesConfig.HOTBAR_HUD.mergeDuplicates;
    }

    public void setMergeDuplicates(final boolean mergeDuplicates) {
        this.mergeDuplicates = mergeDuplicates;
    }

    public void setMetadata(final int meta) {
        this.stack.setItemDamage(meta);
        this.meta = meta;
    }

    public void setDimensionPredicate(@NonNull final IntPredicate predicate) {
        this.dimensionPredicate = predicate;
    }

    public void renewTimestamp() {
        timestamp = TickListener.getTicksElapsed();
    }

    public String getName() {
        return stack.getDisplayName();
    }

    public long getRemainingTicks(final int cooldown) {
        long time = TickListener.getTicksElapsed();
        return (timestamp + cooldown / 50) - time;
    }

    public boolean matchesDimension(final int dimension) {
        return dimensionPredicate.test(dimension);
    }

    // TODO Clean this the fuck up
    public boolean matchesStack(@NonNull final ItemStack stack, final boolean fuzzy) {
        val match = stack.copy();

        match.setCount(1);

        if (!fuzzy) {
            val isItemEqual = ignoreDmg
                              ? ItemStack.areItemsEqualIgnoreDurability(this.stack, match)
                              : ItemStack.areItemsEqual(this.stack, match);
            val isNBTEqual = ignoreNBT || ItemStack.areItemStackTagsEqual(this.stack, match);
            return isItemEqual && isNBTEqual;
        } else if (this.meta == OreDictionary.WILDCARD_VALUE) {
            return this.stack.getItem() == match.getItem();
        } else return ItemStack.areItemsEqualIgnoreDurability(this.stack, match);
    }
}
