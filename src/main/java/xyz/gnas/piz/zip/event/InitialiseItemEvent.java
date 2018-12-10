package xyz.gnas.piz.zip.event;

import java.io.File;

/**
 * Event raised to initialise a zip item
 */
public class InitialiseItemEvent extends ZipEvent {
    private boolean isObfuscated;

    public InitialiseItemEvent(File file, boolean isObfuscated) {
        super(file, ZipEventType.Initialise);
        this.isObfuscated = isObfuscated;
    }

    public boolean isObfuscated() {
        return isObfuscated;
    }
}
