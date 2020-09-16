package net.sleeplessdev.smarthud.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.HandSide;
import net.sleeplessdev.smarthud.config.ModulesConfig;

public class HandHelper {

    /**
     * Used to automatically adjust element offset on the screen depending on
     * the current game setting for the player's main hand. This aids in supporting
     * left-handed mode, and avoiding conflicts with the vanilla HUD elements.
     * @param currentOffset The current offset of the HUD element (averaged from the screen center)
     * @param objectWidth   The current width of the element, used when inverting the position to the negative
     * @return The new offset depending on the current game setting
     */
    public static float getSideOffset(final float currentOffset, final float objectWidth) {
        float newOffset = currentOffset + ModulesConfig.xOffset.get();
        boolean leftHand = Minecraft.getInstance().gameSettings.mainHand == HandSide.LEFT;
        if (leftHand) {
            newOffset = -newOffset;
            newOffset -= objectWidth;
        }
        return newOffset;
    }

    public static boolean isLeftHanded() {
        return Minecraft.getInstance().gameSettings.mainHand == HandSide.LEFT;
    }
}
