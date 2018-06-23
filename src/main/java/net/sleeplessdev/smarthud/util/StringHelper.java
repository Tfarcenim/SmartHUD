package net.sleeplessdev.smarthud.util;

import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class StringHelper {
    /**
     * Abbreviates a number with a suffix after a certain length, e.g. 1500 -> 1.5k
     * @param value The number you want to abbreviate
     * @return The abbreviated number
     */
    public String getAbbreviatedValue(final int value) {
        val builder = new StringBuilder();
        val magnitude = (int) Math.floor(Math.log(value) / Math.log(1000.0D));
        val num = (int) (value / Math.pow(1000.0D, magnitude) * 10.0D);
        val integer = num / 10;
        val fractional = num % 10;

        builder.append(integer);

        if (integer < 10 && fractional > 0) {
            builder.append('.').append(fractional);
        }

        if (magnitude > 0) {
            builder.append("kmbtpe".charAt(magnitude - 1));
        }

        return builder.toString();
    }
}
