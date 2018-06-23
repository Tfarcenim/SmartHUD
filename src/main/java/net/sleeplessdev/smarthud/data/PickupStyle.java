package net.sleeplessdev.smarthud.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PickupStyle {
    BOTH(true, true),
    ICON_ONLY(true, false),
    NAME_ONLY(false, true);

    private final boolean hasIcon;
    private final boolean hasLabel;
}
