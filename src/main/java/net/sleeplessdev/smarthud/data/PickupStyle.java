package net.sleeplessdev.smarthud.data;

public enum PickupStyle {
    BOTH(true, true),
    ICON_ONLY(true, false),
    NAME_ONLY(false, true);

    public final boolean hasIcon;
    public final boolean hasLabel;

    PickupStyle(boolean hasIcon, boolean hasLabel) {
        this.hasIcon = hasIcon;
        this.hasLabel = hasLabel;
    }
}
