package net.sleeplessdev.smarthud.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@UtilityClass
@SideOnly(Side.CLIENT)
public class HandHelper {
    @NonNull
    public EnumHandSide getMainHand() {
        return Minecraft.getMinecraft().gameSettings.mainHand;
    }

    public boolean isLeftHanded() {
        return getMainHand() == EnumHandSide.LEFT;
    }

    /**
     * Used to automatically adjust element offset on the screen depending on
     * the current game setting for the player's main hand. This aids in supporting
     * left-handed mode, and avoiding conflicts with the vanilla HUD elements.
     * @param currentOffset The current offset of the HUD element (averaged from the screen center)
     * @param objectWidth   The current width of the element, used when inverting the position to the negative
     * @return The new offset depending on the current game setting
     */
    public float getSideOffset(final float currentOffset, final float objectWidth) {
        float offset = currentOffset;
        float newOffset = 0.0F;

        if (isLeftHanded()) {
            offset = -currentOffset;
            newOffset = -objectWidth;
        }

        return offset + newOffset;
    }
}
