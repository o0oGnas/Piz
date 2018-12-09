package xyz.gnas.piz.zip.event;

import java.io.File;

public class InitialiseItemEvent extends ZipEvent {
    private boolean isObfuscated;

    public InitialiseItemEvent(File file, boolean isObfuscated) {
        super(file);
        this.isObfuscated = isObfuscated;
    }

    public boolean isObfuscated() {
        return isObfuscated;
    }
}
