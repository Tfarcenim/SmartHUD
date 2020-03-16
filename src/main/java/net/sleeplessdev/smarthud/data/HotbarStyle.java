package net.sleeplessdev.smarthud.data;

public enum HotbarStyle {
    OFFHAND(0),
    HOTBAR(22),
    INVISIBLE(-1);

    HotbarStyle(int textureY){
        this.textureY = textureY;
    }
    public final int textureY;
}
