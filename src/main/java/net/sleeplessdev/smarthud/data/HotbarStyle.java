package net.sleeplessdev.smarthud.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HotbarStyle {
    OFFHAND(0),
    HOTBAR(22),
    INVISIBLE(-1);

    private final int textureY;
}
