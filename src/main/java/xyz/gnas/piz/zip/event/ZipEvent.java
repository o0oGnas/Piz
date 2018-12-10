package xyz.gnas.piz.zip.event;

import java.io.File;

/**
 * Events used to communicate between ZipController and ZipItemController
 */
public class ZipEvent {
    /**
     * Type of the event
     */
    public enum ZipEventType {
        Initialise, Begin, Update, Finish
    }

    private File file;

    private ZipEventType type;

    public File getFile() {
        return file;
    }

    public ZipEventType getType() {
        return type;
    }

    public ZipEvent(File file, ZipEventType type) {
        this.file = file;
        this.type = type;
    }
}