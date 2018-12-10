package xyz.gnas.piz.zip.event;

import xyz.gnas.piz.zip.model.ZipProcessModel;

import java.io.File;

/**
 * Event raised to update progress of a zip item
 */
public class UpdateProgressEvent extends ZipEvent {
    private ZipProcessModel process;

    public UpdateProgressEvent(File file, ZipProcessModel process) {
        super(file, ZipEventType.Update);
        this.process = process;
    }

    public ZipProcessModel getProcess() {
        return process;
    }
}
