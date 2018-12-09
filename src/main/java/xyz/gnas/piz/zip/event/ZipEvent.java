package xyz.gnas.piz.zip.event;

import java.io.File;

public abstract class ZipEvent {
    private File file;

    ZipEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}